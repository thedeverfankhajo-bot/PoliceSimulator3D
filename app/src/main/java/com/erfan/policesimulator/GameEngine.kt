package com.erfan.policesimulator

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

internal data class GameSnapshot(val speed: Int, val lights: Boolean, val siren: Boolean, val onFoot: Boolean, val mission: String, val objective: String, val distance: Int, val score: Int, val radio: String, val decisionRequired: Boolean, val career: CareerProgress, val vehicleCondition: Int, val credits: Int, val garageDistance: Int, val worldClock: String, val worldPhase: String)

internal class GameEngine(context: Context) : GLSurfaceView.Renderer {
    @Volatile private var gas=false
    @Volatile private var brake=false
    @Volatile private var steering=0f
    @Volatile private var interact=false
    @Volatile private var nextCall=false
    @Volatile private var vehicleToggle=false
    @Volatile private var sirenToggle=false
    @Volatile private var arrestDecision=0
    @Volatile private var x=0f
    @Volatile private var z=12f
    @Volatile private var angle=0f
    @Volatile private var speed=0f
    @Volatile private var vehicleCondition=VehicleDamageRules.MAX_CONDITION
    @Volatile private var credits=1_000
    @Volatile private var garageRepairRequested=false
    @Volatile private var garageMessage="Garage: repair service available near the station."
    @Volatile private var repairAnimationUntil=0L
    @Volatile private var worldTimeSeconds=19f*3_600f
    @Volatile private var worldLighting=DayNightCycle.at(19f*3_600f)
    @Volatile private var nextDamageNoticeAt=0L
    @Volatile private var lights=false
    @Volatile private var siren=false
    @Volatile private var onFoot=false
    @Volatile private var stage=0
    @Volatile private var career = CareerProgress()
    @Volatile private var careerUpdated = false
    @Volatile private var suspectX=2.5f
    @Volatile private var suspectZ=-38f
    @Volatile private var warrant=true
    @Volatile private var callIndex=0
    @Volatile private var activeCallout: DispatchCallout?=null
    @Volatile private var pursuitActive=false
    private var suspectRoute: List<RoadNode> = emptyList()
    private var suspectRouteIndex = 1
    private var suspectAngle = 0f
    @Volatile private var radio="Dispatch: Unit 12 ready for patrol."
    @Volatile private var objective="Accept a call, activate lights and siren, then approach the suspect."
    @Volatile private var message="Welcome, Officer. Accept the dispatch call."
    @Volatile private var messageUntil=SystemClock.uptimeMillis()+4000L
    @Volatile private var listener: ((GameSnapshot,String)->Unit)?=null
    private val applicationContext = context.applicationContext
    private val careerProfileStore = CareerProfileStore(applicationContext)
    private val patrolAudio = PatrolAudio()

    init {
        careerProfileStore.loadProfile(
            onLoaded = { savedProfile ->
                if (!careerUpdated) career = savedProfile.career
                vehicleCondition = savedProfile.vehicleCondition
                credits = savedProfile.credits
            },
            onError = {
                announce("Career and vehicle profile could not be loaded.", SystemClock.uptimeMillis())
            }
        )
    }
    private var patrolX=0f
    private var patrolZ=12f
    private var lastFrame=0L
    private var lastUi=0L
    private var program=0
    private var posLoc=0
    private var colorLoc=0
    private var mvpLoc=0
    private var vertexCount=0
    private lateinit var cube: FloatBuffer
    private val projection=FloatArray(16)
    private val view=FloatArray(16)
    private val vp=FloatArray(16)
    private val model=FloatArray(16)
    private val mvp=FloatArray(16)
    private val boxes=ArrayList<Box>()
    private val traffic=ArrayList<Car>()
    private val buildingColliders=ArrayList<CollisionRect>()
    private val roadPlanes=ArrayList<RoadPlane>()
    private val streetLamps=ArrayList<StreetLamp>()
    private var textureProgram=0
    private var texturePositionLoc=0
    private var textureUvLoc=0
    private var textureMvpLoc=0
    private var textureSamplerLoc=0
    private var textureTintLoc=0
    private var asphaltTexture=0
    private var concreteTexture=0
    private lateinit var texturedCube:FloatBuffer
    private var texturedVertexCount=0
    private var nextCollisionNoticeAt=0L
    private var viewportWidth=1
    private var viewportHeight=1
    private data class Box(val x:Float,val y:Float,val z:Float,val sx:Float,val sy:Float,val sz:Float,val color:FloatArray,val textureKind:Int=0,val nightEmissive:Boolean=false)
    private data class RoadPlane(val vertices:FloatBuffer)
    private data class StreetLamp(val x:Float,val z:Float)
    private data class Car(var x:Float,var z:Float,val angle:Float,val speed:Float,val color:FloatArray,val verticalRoad:Boolean,var shoulderOffset:Float=0f,var speedFactor:Float=1f)

    fun setListener(value:(GameSnapshot,String)->Unit){listener=value}
    fun resumeAudio(){patrolAudio.setActive(true)}
    fun pauseAudio(){patrolAudio.setActive(false)}
    fun releaseAudio(){patrolAudio.release()}
    fun gas(value:Boolean){gas=value}
    fun brake(value:Boolean){brake=value}
    fun steer(value:Float){steering=value.coerceIn(-1f,1f)}
    fun interact(){interact=true}
    fun requestCall(){nextCall=true}
    fun requestGarageRepair(){garageRepairRequested=true}
    fun toggleVehicle(){vehicleToggle=true}
    fun toggleSiren(){sirenToggle=true}
    fun chooseArrest(){arrestDecision=1}
    fun chooseCitation(){arrestDecision=-1}
    fun snapshot()=GameSnapshot((abs(speed)*3.6f).toInt(),lights,siren,onFoot,missionName(),objective,distance(x,z,suspectX,suspectZ).toInt(),career.xp,radio,stage==4,career,vehicleCondition,credits,distance(x,z,GARAGE_X,GARAGE_Z).toInt(),worldLighting.clockText,worldLighting.phase.displayName)

    override fun onSurfaceCreated(gl:GL10?,config:EGLConfig?){
        GLES20.glClearColor(0.43f,0.66f,0.79f,1f);GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        val vs=shader(GLES20.GL_VERTEX_SHADER,VERTEX);val fs=shader(GLES20.GL_FRAGMENT_SHADER,FRAGMENT)
        program=GLES20.glCreateProgram();GLES20.glAttachShader(program,vs);GLES20.glAttachShader(program,fs);GLES20.glLinkProgram(program)
        val status=IntArray(1);GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,status,0)
        check(status[0]==GLES20.GL_TRUE){GLES20.glGetProgramInfoLog(program)}
        posLoc=GLES20.glGetAttribLocation(program,"aPosition");colorLoc=GLES20.glGetUniformLocation(program,"uColor");mvpLoc=GLES20.glGetUniformLocation(program,"uMvp")
        val textureVs=shader(GLES20.GL_VERTEX_SHADER,TEXTURE_VERTEX)
        val textureFs=shader(GLES20.GL_FRAGMENT_SHADER,TEXTURE_FRAGMENT)
        textureProgram=GLES20.glCreateProgram()
        GLES20.glAttachShader(textureProgram,textureVs);GLES20.glAttachShader(textureProgram,textureFs);GLES20.glLinkProgram(textureProgram)
        val textureStatus=IntArray(1);GLES20.glGetProgramiv(textureProgram,GLES20.GL_LINK_STATUS,textureStatus,0)
        check(textureStatus[0]==GLES20.GL_TRUE){GLES20.glGetProgramInfoLog(textureProgram)}
        texturePositionLoc=GLES20.glGetAttribLocation(textureProgram,"aPosition")
        textureUvLoc=GLES20.glGetAttribLocation(textureProgram,"aTexCoord")
        textureMvpLoc=GLES20.glGetUniformLocation(textureProgram,"uMvp")
        textureSamplerLoc=GLES20.glGetUniformLocation(textureProgram,"uTexture")
        textureTintLoc=GLES20.glGetUniformLocation(textureProgram,"uTint")
        cube=makeCube();texturedCube=makeTexturedCube()
        asphaltTexture=loadTexture("textures/asphalt_03_diff_1k.jpg")
        concreteTexture=loadTexture("textures/concrete_floor_diff_1k.jpg")
        buildCity();buildTraffic()
    }
    override fun onSurfaceChanged(gl:GL10?,width:Int,height:Int){
        viewportWidth=max(1,width);viewportHeight=max(1,height);GLES20.glViewport(0,0,viewportWidth,viewportHeight)
        Matrix.perspectiveM(projection,0,58f,viewportWidth.toFloat()/viewportHeight,0.1f,500f)
    }
    override fun onDrawFrame(gl:GL10?){
        val now=SystemClock.uptimeMillis();val dt=if(lastFrame==0L)0.016f else ((now-lastFrame).coerceAtMost(40L)/1000f);lastFrame=now
        worldTimeSeconds=DayNightCycle.advance(worldTimeSeconds,dt)
        worldLighting=DayNightCycle.at(worldTimeSeconds)
        GLES20.glClearColor(worldLighting.skyRed,worldLighting.skyGreen,worldLighting.skyBlue,1f)
        update(dt,now);GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        val back=if(onFoot)5.5f else 9.5f
        Matrix.setLookAtM(view,0,x+sin(angle)*back,if(onFoot)3f else 5f,z+cos(angle)*back,x-sin(angle)*2f,if(onFoot)1f else 0.8f,z-cos(angle)*2f,0f,1f,0f)
        Matrix.multiplyMM(vp,0,projection,0,view,0);GLES20.glUseProgram(program)
        // Distance culling avoids submitting far-away facade/window meshes every frame.
        // Large ground and road boxes remain visible because the helper tests their full footprint.
        for(b in boxes){
            if(!RenderDistance.intersectsCircle(
                    playerX=x, playerZ=z,
                    centerX=b.x, centerZ=b.z,
                    halfWidth=abs(b.sx)*0.5f, halfDepth=abs(b.sz)*0.5f,
                    viewDistance=SCENE_RENDER_DISTANCE
                )) continue
            val visibleColor=if(b.nightEmissive&&worldLighting.streetLightsOn)rgb(0xffcc80)else b.color
            val emissive=b.nightEmissive&&worldLighting.streetLightsOn
            if(b.textureKind==TEXTURE_CONCRETE)drawTexturedBox(b.x,b.y,b.z,b.sx,b.sy,b.sz,0f,concreteTexture,worldTint(brighten(visibleColor)))
            else drawBox(b.x,b.y,b.z,b.sx,b.sy,b.sz,0f,visibleColor,emissive)
        }
        drawRoadPlanes(worldTint(rgb(0xffffff)))
        drawBox(GARAGE_X,0.18f,GARAGE_Z,5.5f,0.12f,5.5f,0f,rgb(0x1c7a55))
        drawBox(GARAGE_X,0.75f,GARAGE_Z,2.3f,0.95f,0.25f,0f,rgb(0xe8f0e8))
        drawBox(GARAGE_X,1.35f,GARAGE_Z,1.8f,0.12f,0.2f,0f,rgb(0xf0c94f))
        for(c in traffic){
            val carX=trafficWorldX(c)
            val carZ=trafficWorldZ(c)
            if(RenderDistance.intersectsCircle(x,z,carX,carZ,1.0f,1.9f,VEHICLE_RENDER_DISTANCE))
                drawCar(carX,carZ,c.angle,c.color,false,now)
        }
        if(stage!=5 && RenderDistance.intersectsCircle(x,z,suspectX,suspectZ,1f,1.9f,VEHICLE_RENDER_DISTANCE))
            drawCar(suspectX,suspectZ,suspectAngle,rgb(activeCallout?.targetVehicleColor ?: 0x9aa5ab),false,now)
        if(!onFoot){
            drawPatrolCar(patrolX,patrolZ,angle,now)
            if(now<repairAnimationUntil) drawMechanic(now)
        } else drawOfficer(now)
        if(now-lastUi>180L){lastUi=now;listener?.invoke(snapshot(),if(now<messageUntil)message else "")}
    }
    private fun update(dt:Float,now:Long){
        if(garageRepairRequested){
            garageRepairRequested=false
            if(onFoot||abs(speed)>=0.5f||distance(x,z,GARAGE_X,GARAGE_Z)>GARAGE_RADIUS){
                garageMessage="Garage unavailable: park inside the marked service area."
                announce(garageMessage,now)
            }else{
                careerProfileStore.repairVehicle(
                    onSaved={profile,result->
                        credits=profile.credits
                        vehicleCondition=profile.vehicleCondition
                        garageMessage=result.message
                        if(result.repaired) repairAnimationUntil=SystemClock.uptimeMillis()+2600L
                        announce(garageMessage,SystemClock.uptimeMillis())
                    },
                    onError={announce("Garage transaction failed. No repair was confirmed.",SystemClock.uptimeMillis())}
                )
                announce("Garage: processing repair quote…",now)
            }
        }
        if(sirenToggle){sirenToggle=false;siren=!siren;lights=siren;radio=if(siren)"Dispatch: emergency equipment activated." else "Dispatch: emergency equipment off."}
        if(vehicleToggle){vehicleToggle=false
            if(!onFoot&&abs(speed)<1.5f){onFoot=true;speed=0f;x+=cos(angle)*1.7f;z-=sin(angle)*1.7f;announce("Officer on foot.",now)}
            else if(onFoot&&distance(x,z,patrolX,patrolZ)<5f){onFoot=false;x=patrolX;z=patrolZ;announce("Back in patrol vehicle.",now)}
            else announce(if(onFoot)"Move closer to your patrol car." else "Stop before exiting.",now)
        }
        if(nextCall){nextCall=false
            if(stage==0||stage==5){
                stage=1
                val callout=DispatchCalloutFactory.create(callIndex++, now)
                activeCallout=callout
                suspectX=(kotlin.math.round(x/40f)*40f)+2.5f
                suspectZ=(kotlin.math.round((z-65f)/40f)*40f)+2f
                pursuitActive=false;suspectRoute=emptyList();suspectRouteIndex=1;suspectAngle=0f
                warrant=callout.hasActiveWarrant
                objective=callout.approachObjective
                radio=callout.dispatchMessage
                announce("Call accepted: ${callout.title}.",now)
            }
            else announce("Complete the current call first.",now)
        }
        if(arrestDecision!=0){
            val selected=arrestDecision
            arrestDecision=0
            handleDecision(selected==1,now)
        }
        if(!onFoot){if(gas)speed+=12f*VehicleDamageRules.accelerationMultiplier(vehicleCondition)*dt else if(brake)speed-=19f*dt else speed*=max(0f,1f-0.8f*dt)
            val maxSpeed=VehicleDamageRules.maximumSpeed(vehicleCondition)
            speed=speed.coerceIn(-min(6f,maxSpeed*0.3f),maxSpeed);angle+=steering*dt*(0.3f+min(abs(speed)/12f,1f))*if(speed>=0f)1f else -1f
            val movement=WorldCollision.move(x,z,-sin(angle)*speed*dt,-cos(angle)*speed*dt,1.55f,buildingColliders)
            x=movement.x;z=movement.z
            if(movement.collided){
                speed*=0.58f
                damagePatrolVehicle(now,trafficImpact=false)
                patrolAudio.playImpact()
                reportCollision(now,false)
            }
            patrolX=x;patrolZ=z
        }else{
            angle+=steering*dt*1.8f
            val move=if(gas)1f else if(brake)-1f else 0f
            val movement=WorldCollision.move(x,z,-sin(angle)*move*4f*dt,-cos(angle)*move*4f*dt,0.42f,buildingColliders)
            x=movement.x;z=movement.z
            if(movement.collided&&move!=0f){patrolAudio.playImpact();reportCollision(now,true)}
        }
        // Traffic yields only to an approaching patrol vehicle using both emergency lights and siren.
        // Lane motion and speed response are smoothed so vehicles do not snap or teleport.
        val emergencyPriority = lights && siren && !onFoot
        traffic.forEach { car ->
            val carX = trafficWorldX(car)
            val carZ = trafficWorldZ(car)
            val distanceToPatrol = distance(x, z, carX, carZ)
            val yielding = TrafficBehavior.shouldYield(
                emergencyActive = emergencyPriority,
                distance = distanceToPatrol
            )
            val lanePosition = if (car.verticalRoad) car.x else car.z
            val roadCenter = kotlin.math.round(lanePosition / ROAD_SPACING) * ROAD_SPACING
            car.shoulderOffset = TrafficBehavior.approachShoulderOffset(
                currentOffset = car.shoulderOffset,
                lanePosition = lanePosition,
                roadCenter = roadCenter,
                yielding = yielding,
                deltaSeconds = dt
            )
            car.speedFactor = TrafficBehavior.approachSpeedFactor(
                currentFactor = car.speedFactor,
                yielding = yielding,
                deltaSeconds = dt
            )

            val trafficSpeed = car.speed * car.speedFactor
            if (car.verticalRoad) {
                car.z += -cos(car.angle) * trafficSpeed * dt
                if (car.z < -165f) car.z = 165f
                if (car.z > 165f) car.z = -165f
            } else {
                car.x += -sin(car.angle) * trafficSpeed * dt
                if (car.x < -165f) car.x = 165f
                if (car.x > 165f) car.x = -165f
            }
        }

        // Resolve contacts using the rendered, shoulder-adjusted traffic positions.
        var trafficContact = false
        val contactRadius = if (onFoot) 0.42f else 1.55f
        for (car in traffic) {
            val contact = TrafficCollisionRules.resolve(
                playerX = x,
                playerZ = z,
                trafficX = trafficWorldX(car),
                trafficZ = trafficWorldZ(car),
                trafficMovesVertically = car.verticalRoad,
                radius = contactRadius
            )
            if (contact.collided) {
                x = contact.x
                z = contact.z
                trafficContact = true
            }
        }
        if (trafficContact) {
            if (!onFoot) {
                speed *= 0.35f
                patrolX = x
                patrolZ = z
            }
            if(!onFoot) damagePatrolVehicle(now,trafficImpact=true)
            patrolAudio.playImpact()
            reportCollision(now, onFoot)
        }
        if(stage==1){
            var suspectDistance=distance(x,z,suspectX,suspectZ)
            val callout=activeCallout
            val withinContactRange=suspectDistance<=PursuitRules.START_FLEE_DISTANCE
            if(callout?.suspectResponse==SuspectResponse.COMPLIES&&lights&&siren&&!onFoot&&withinContactRange&&!pursuitActive){
                pursuitActive=false
                stage=2
                suspectRoute=emptyList()
                objective=callout.complianceObjective
                radio=callout.complianceRadioMessage
                announce("Driver has complied. Complete the stop.",now)
            }else if(callout?.suspectResponse==SuspectResponse.FLEES&&lights&&siren&&!onFoot&&suspectDistance<=PursuitRules.START_FLEE_DISTANCE&&!pursuitActive){
                pursuitActive=true
                suspectRoute=StreetRoutePlanner.planEscapeRoute(
                    startX=suspectX,
                    startZ=suspectZ,
                    patrolX=x,
                    patrolZ=z,
                    obstacles=buildingColliders
                )
                suspectRouteIndex=1
                suspectRoute.firstOrNull()?.let { first ->
                    suspectX=first.worldX
                    suspectZ=first.worldZ
                }
                announce("Suspect fleeing. Pursuit route calculated.",now)
            }

            suspectDistance=distance(x,z,suspectX,suspectZ)
            if(suspectDistance<=PursuitRules.STOP_DISTANCE&&abs(speed)<=PursuitRules.STOP_PATROL_SPEED){
                stage=2
                pursuitActive=false
                suspectRoute=emptyList()
                objective="Suspect stopped. Exit the patrol car and approach on foot."
                radio="Dispatch: suspect vehicle stopped. Proceed with field contact."
                announce("Vehicle stopped. Conduct a field contact.",now)
            }else if(pursuitActive){
                if(suspectRoute.size<2){
                    stage=5
                    pursuitActive=false
                    objective="No clear escape route. Request a new call when ready."
                    radio="Dispatch: pursuit ended. Unit 12 return to patrol."
                    announce("Pursuit route unavailable. Call closed.",now)
                }else{
                    val progress=StreetRoutePlanner.advanceAlongRoute(
                        route=suspectRoute,
                        routeIndex=suspectRouteIndex,
                        currentX=suspectX,
                        currentZ=suspectZ,
                        speed=PursuitRules.FLEE_SPEED_FACTOR*8f,
                        deltaSeconds=dt
                    )
                    suspectX=progress.x
                    suspectZ=progress.z
                    suspectRouteIndex=progress.routeIndex
                    suspectAngle=progress.headingRadians
                    suspectDistance=distance(x,z,suspectX,suspectZ)

                    if(suspectDistance>=PursuitRules.ESCAPE_DISTANCE){
                        stage=5
                        pursuitActive=false
                        suspectRoute=emptyList()
                        objective="Suspect escaped. Request a new call when ready."
                        radio="Dispatch: suspect lost. Unit 12, return to patrol."
                        announce("Suspect escaped. Call closed.",now)
                    }else if(progress.finished){
                        suspectRoute=StreetRoutePlanner.planEscapeRoute(
                            startX=suspectX,
                            startZ=suspectZ,
                            patrolX=x,
                            patrolZ=z,
                            obstacles=buildingColliders
                        )
                        suspectRouteIndex=1
                        if(suspectRoute.size<2){
                            stage=5
                            pursuitActive=false
                            objective="Suspect reached a dead end. Request a new call when ready."
                            radio="Dispatch: pursuit ended. Unit 12 return to patrol."
                            announce("Suspect route ended. Call closed.",now)
                        }
                    }
                    if(stage==1){
                        objective=callout?.pursuitObjective ?: "Pursuit active. Follow the suspect along city streets and close the distance."
                        radio=if(callout?.type==DispatchType.STOLEN_VEHICLE)
                            "Dispatch: stolen vehicle pursuit active. Backup requested."
                        else "Dispatch: vehicle pursuit active. Requesting backup."
                    }
                }
            }else if(stage==1){
                objective=callout?.approachObjective
                    ?: "Approach the suspicious vehicle. Activate lights and siren to initiate pursuit, or stop nearby to conduct a contact."
            }
        }
        patrolAudio.update(speed,siren,onFoot&&(gas||brake))
        if(interact){interact=false;handleInteraction(now)}
    }
    private fun handleInteraction(now:Long){when(stage){
        0->announce("Accept a dispatch call first.",now)
        1->announce("Activate lights and siren and approach the suspect.",now)
        2->if(onFoot&&distance(x,z,suspectX,suspectZ)<6f){stage=3;objective="Interact again to request licence and warrant records.";radio="Dispatch: field contact started.";announce("Driver contact started.",now)}else announce("Exit the vehicle and move closer.",now)
        3->if(onFoot&&distance(x,z,suspectX,suspectZ)<8f){
            stage=4
            objective=if(warrant)"Active warrant confirmed. Choose ARREST or CITE / WARN." else "No active warrant. Choose CITE / WARN; do not arrest without legal grounds."
            radio=if(warrant)"Dispatch: active warrant confirmed." else "Dispatch: no active warrant."
            announce(if(warrant)"Records returned an active warrant." else "Records returned no active warrant.",now)
        }else announce("Stay near the driver to request records.",now)
        4->announce("Choose ARREST or CITE / WARN using the action buttons.",now)
        else->announce("Request the next call.",now)
    }}
    private fun handleDecision(arrest:Boolean,now:Long){
        if(stage!=4){announce("Check the driver's records before choosing an outcome.",now);return}
        val action=if(arrest) IncidentAction.ARREST else IncidentAction.CITE_OR_WARN
        val outcome=IncidentRules.resolve(warrant,action)
        careerUpdated = true
        careerProfileStore.recordDecision(
            outcome = outcome,
            onSaved = { savedProfile -> career = savedProfile },
            onError = { announce("Career save failed. Check available storage.", SystemClock.uptimeMillis()) }
        )
        stage=5
        lights=false
        siren=false
        objective="Call complete. Request another dispatch assignment."
        radio=outcome.dispatchMessage
        announce(outcome.message,now)
    }
    private fun missionName()=when(stage){0->"Dispatch available";1->"En route: vehicle stop";2->"Suspect stopped";3->"Field contact";4->"Records checked";else->"Call complete"}
    private fun announce(text:String,now:Long){
        message=text
        messageUntil=now+2800L
        patrolAudio.playRadioChirp()
    }
    private fun damagePatrolVehicle(now:Long,trafficImpact:Boolean){
        val previous=vehicleCondition
        vehicleCondition=VehicleDamageRules.applyImpact(previous,abs(speed),trafficImpact)
        if(vehicleCondition<previous){
            careerProfileStore.saveVehicle(
                condition=vehicleCondition,
                onSaved={profile->
                    vehicleCondition=profile.vehicleCondition
                    credits=profile.credits
                },
                onError={announce("Vehicle damage could not be saved.",SystemClock.uptimeMillis())}
            )
        }
        if(vehicleCondition<previous&&now>=nextDamageNoticeAt){
            nextDamageNoticeAt=now+1800L
            val level=when{
                vehicleCondition<25->"CRITICAL"
                vehicleCondition<50->"HEAVY"
                vehicleCondition<75->"MODERATE"
                else->"LIGHT"
            }
            announce("Patrol vehicle damage: $level. Condition $vehicleCondition%.",now)
        }
    }
    private fun drawPatrolCar(carX:Float,carZ:Float,heading:Float,now:Long){
        val damage=1f-vehicleCondition/100f
        drawCar(carX,carZ,heading,rgb(0x1d4777),true,now)
        if(damage<0.08f)return
        // Damage is intentionally geometric and procedural: no copyrighted vehicle texture is copied.
        val frontX=carX-sin(heading)*1.45f
        val frontZ=carZ-cos(heading)*1.45f
        val rearX=carX+sin(heading)*1.45f
        val rearZ=carZ+cos(heading)*1.45f
        val dentColor=rgb(0x343b43)
        val scrapeColor=rgb(0xb5b8bb)
        if(damage>=0.15f){
            drawBox(frontX,0.47f,frontZ,0.64f,0.16f,0.12f,heading,dentColor)
            drawBox(rearX,0.43f,rearZ,0.72f,0.12f,0.10f,heading,dentColor)
        }
        if(damage>=0.35f){
            drawBox(carX-0.88f,0.52f,carZ,0.12f,0.18f,0.72f,heading,scrapeColor)
            drawBox(carX+0.88f,0.52f,carZ,0.12f,0.18f,0.72f,heading,dentColor)
            drawBox(frontX,0.72f,frontZ,0.44f,0.18f,0.05f,heading,rgb(0x9ca4ab))
        }
        if(damage>=0.65f){
            drawBox(carX-0.43f,0.91f,carZ-0.18f,0.43f,0.06f,0.34f,heading,rgb(0x4a535a))
            drawBox(carX+0.43f,0.91f,carZ+0.18f,0.43f,0.06f,0.34f,heading,rgb(0x4a535a))
            // Subtle engine smoke is emitted only while the vehicle is moving and badly damaged.
            if(abs(speed)>0.4f){
                val pulse=0.25f+0.12f*sin(now/140.0).toFloat()
                drawBox(frontX,1.15f+pulse,frontZ,0.34f,0.25f,0.34f,heading,rgb(0x7c858b))
            }
        }
    }
    private fun drawMechanic(now:Long){
        val phase=((now%700L)/700f)*2f*PI.toFloat()
        val armLift=0.22f+0.13f*sin(phase)
        val mx=patrolX+1.65f
        val mz=patrolZ+0.25f
        drawBox(mx,0.72f,mz,0.34f,0.62f,0.25f,0f,rgb(0xf0a52b))
        drawBox(mx,1.14f,mz,0.28f,0.25f,0.28f,0f,rgb(0xd8b08a))
        drawBox(mx-0.19f,0.79f-armLift,mz-0.02f,0.11f,0.42f,0.11f,-0.45f,rgb(0xf0a52b))
        drawBox(mx+0.19f,0.79f+armLift,mz-0.02f,0.11f,0.42f,0.11f,0.45f,rgb(0xf0a52b))
        val pulse=0.35f+0.18f*sin(phase*2f)
        drawBox(patrolX,0.12f,patrolZ,2.7f,0.035f,4.4f,0f,rgb(0x4ce0a0,pulse))
        drawBox(patrolX,0.06f,patrolZ,3.2f,0.025f,4.9f,0f,rgb(0xf2cc56,0.45f))
    }
    private fun reportCollision(now:Long,onFoot:Boolean){
        if(now<nextCollisionNoticeAt)return
        nextCollisionNoticeAt=now+1500L
        announce(if(onFoot)"A building blocks your path." else "Impact detected. Patrol vehicle slowed.",now)
    }
    private fun distance(ax:Float,az:Float,bx:Float,bz:Float):Float{val dx=ax-bx;val dz=az-bz;return kotlin.math.sqrt(dx*dx+dz*dz)}
    private fun trafficWorldX(car:Car)=car.x+(if(car.verticalRoad)car.shoulderOffset else 0f)
    private fun trafficWorldZ(car:Car)=car.z+(if(car.verticalRoad)0f else car.shoulderOffset)
    private fun buildCity(){
        boxes.clear();buildingColliders.clear();roadPlanes.clear();boxes.add(Box(0f,-0.3f,0f,360f,0.5f,360f,rgb(0x626a63)));val roads=(-3..3).map{it*40f}
        roads.forEach{rx->boxes.add(Box(rx,0f,0f,12f,0.08f,340f,rgb(0x30383e)));boxes.add(Box(rx-6.2f,0.08f,0f,0.8f,0.16f,340f,rgb(0x929898)));boxes.add(Box(rx+6.2f,0.08f,0f,0.8f,0.16f,340f,rgb(0x929898)));for(rz in -160..160 step 20)boxes.add(Box(rx,0.06f,rz.toFloat(),0.13f,0.02f,4f,rgb(0xe8c967)))}
        roads.forEach{rz->boxes.add(Box(0f,0f,rz,340f,0.08f,12f,rgb(0x30383e)));boxes.add(Box(0f,0.08f,rz-6.2f,340f,0.16f,0.8f,rgb(0x929898)));boxes.add(Box(0f,0.08f,rz+6.2f,340f,0.16f,0.8f,rgb(0x929898)))}
        roads.forEach{rx->roadPlanes.add(makeRoadPlane(rx,0.043f,12f,340f))}
        roads.forEach{rz->roadPlanes.add(makeRoadPlane(0f,0.043f,340f,12f,rz))}
        val colors=intArrayOf(0x626e79,0x737e83,0x817b71,0x586a76,0x777f76);var i=0
        for(gx in -100..100 step 40)for(gz in -100..100 step 40){val bx=gx+(if((i/6)%2==0)12f else -12f)+(i%7-3)*0.35f;val bz=gz+(if(i%2==0)12f else -12f)+(i%5-2)*0.4f;val w=10f+(i%4)*1.7f;val d=10f+(i%3)*2f;val h=9f+(i%6)*3.2f
            buildingColliders.add(CollisionRect(bx,bz,w/2f,d/2f))
            boxes.add(Box(bx,h/2f,bz,w,h,d,rgb(colors[i++%colors.size]),TEXTURE_CONCRETE));boxes.add(Box(bx,h+0.1f,bz,w+0.2f,0.2f,d+0.2f,rgb(0x3f4c55)))
            for(row in 0..3)for(col in -1..1){val wy=1.6f+row*(h-2f)/4f;boxes.add(Box(bx+col*w*0.27f,wy,bz-d/2f-0.03f,w*0.11f,0.72f,0.05f,rgb(0x8bb6c1)));boxes.add(Box(bx+col*w*0.27f,wy,bz+d/2f+0.03f,w*0.11f,0.72f,0.05f,rgb(0x8bb6c1)))}
        }
        for(lx in -120..120 step 80)for(lz in -120..120 step 80){boxes.add(Box(lx+8f,2.1f,lz+8f,0.15f,4.2f,0.15f,rgb(0x4b565e)));boxes.add(Box(lx+8f,4.15f,lz+7.4f,0.2f,0.14f,1.2f,rgb(0xded5a7)))}
    }
    private fun buildTraffic(){traffic.clear();repeat(10){i->val vertical=i%2==0;val lane=((i%7)-3)*40f+if(i%3==0)2.5f else -2.5f;val a=if(vertical){if(i%4==0)0f else Math.PI.toFloat()}else{if(i%4==0)(Math.PI/2).toFloat()else(-Math.PI/2).toFloat()};val c=intArrayOf(0xcbd1d3,0x9aa8b0,0x718891,0xb5a68e)[i%4];traffic.add(if(vertical)Car(lane,(i*29-130).toFloat(),a,4f+(i%4),rgb(c),true)else Car((i*31-140).toFloat(),lane,a,4f+(i%4),rgb(c),false))}}
    private fun drawOfficer(now:Long){
        val walking=gas||brake
        val gait=if(walking)sin(now*0.014f)*0.24f else 0f
        val bob=if(walking)abs(sin(now*0.028f))*0.055f else 0f
        drawBox(x,1f+bob,z,0.52f,1f,0.38f,angle,rgb(0x17365c))
        drawBox(x,1.65f+bob,z,0.36f,0.36f,0.34f,angle,rgb(0xd1a17e))
        drawBox(x,1.86f+bob,z,0.39f,0.12f,0.38f,angle,rgb(0x152b45))
        for(side in intArrayOf(-1,1)){
            val lx=side*0.16f;val lz=gait*side
            val legX=x+lx*cos(angle)+lz*sin(angle);val legZ=z-lx*sin(angle)+lz*cos(angle)
            drawBox(legX,0.35f,legZ,0.24f,0.62f,0.27f,angle,rgb(0x17243a))
            drawBox(legX,0.07f,legZ-0.05f,0.29f,0.13f,0.45f,angle,rgb(0x161b21))
            val armZ=-lz*0.85f;val armX=x+side*0.39f*cos(angle)+armZ*sin(angle);val armWorldZ=z-side*0.39f*sin(angle)+armZ*cos(angle)
            drawBox(armX,1.05f+bob,armWorldZ,0.2f,0.66f,0.22f,angle,rgb(0x17365c))
        }
    }
    private fun drawCar(cx:Float,cz:Float,a:Float,paint:FloatArray,police:Boolean,now:Long){
        drawBox(cx,0.52f,cz,1.8f,0.5f,3.55f,a,paint);drawBox(cx,0.91f,cz,1.18f,0.48f,1.75f,a,if(police)rgb(0xe1e7e9)else rgb(0xbac5c8))
        for(side in intArrayOf(-1,1))for(end in intArrayOf(-1,1)){val lx=side*0.92f;val lz=end*1.05f;drawBox(cx+lx*cos(a)+lz*sin(a),0.32f,cz-lx*sin(a)+lz*cos(a),0.28f,0.48f,0.58f,a,rgb(0x171c21))}
        if(police){drawBox(cx,1.2f,cz,0.8f,0.12f,0.28f,a,rgb(0x17202a));val flash=now/230L%2L==0L;drawBox(cx-0.23f,1.3f,cz,0.32f,0.14f,0.22f,a,rgb(if(lights&&flash)0xff394b else 0x7d202a));drawBox(cx+0.23f,1.3f,cz,0.32f,0.14f,0.22f,a,rgb(if(lights&&!flash)0x235dff else 0x20356d))}
    }
    private fun loadTexture(assetPath:String):Int{
        val bitmap=applicationContext.assets.open(assetPath).use{BitmapFactory.decodeStream(it)}
            ?: error("Unable to decode texture asset: $assetPath")
        val ids=IntArray(1)
        GLES20.glGenTextures(1,ids,0)
        check(ids[0]!=0){"Unable to allocate OpenGL texture for $assetPath"}
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,ids[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0)
        bitmap.recycle()
        return ids[0]
    }
    private fun makeRoadPlane(cx:Float,y:Float,width:Float,depth:Float,cz:Float=0f):RoadPlane{
        val left=cx-width/2f;val right=cx+width/2f;val near=cz-depth/2f;val far=cz+depth/2f
        val u=width/5f;val v=depth/5f
        val values=floatArrayOf(
            left,y,near,0f,0f, right,y,near,u,0f, right,y,far,u,v,
            left,y,near,0f,0f, right,y,far,u,v, left,y,far,0f,v
        )
        return RoadPlane(ByteBuffer.allocateDirect(values.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply{put(values);position(0)})
    }
    private fun drawRoadPlanes(tint:FloatArray){
        if(asphaltTexture==0||roadPlanes.isEmpty())return
        GLES20.glUseProgram(textureProgram)
        GLES20.glUniformMatrix4fv(textureMvpLoc,1,false,vp,0)
        GLES20.glUniform1i(textureSamplerLoc,0)
        GLES20.glUniform3fv(textureTintLoc,1,tint,0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,asphaltTexture)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0)
        GLES20.glEnableVertexAttribArray(texturePositionLoc);GLES20.glEnableVertexAttribArray(textureUvLoc)
        for(plane in roadPlanes){
            plane.vertices.position(0)
            GLES20.glVertexAttribPointer(texturePositionLoc,3,GLES20.GL_FLOAT,false,20,plane.vertices)
            plane.vertices.position(3)
            GLES20.glVertexAttribPointer(textureUvLoc,2,GLES20.GL_FLOAT,false,20,plane.vertices)
            plane.vertices.position(0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6)
        }
        GLES20.glDisableVertexAttribArray(texturePositionLoc);GLES20.glDisableVertexAttribArray(textureUvLoc)
    }
    private fun drawTexturedBox(cx:Float,cy:Float,cz:Float,sx:Float,sy:Float,sz:Float,a:Float,textureId:Int,tint:FloatArray){
        Matrix.setIdentityM(model,0);Matrix.translateM(model,0,cx,cy,cz);Matrix.rotateM(model,0,Math.toDegrees(a.toDouble()).toFloat(),0f,1f,0f);Matrix.scaleM(model,0,sx,sy,sz);Matrix.multiplyMM(mvp,0,vp,0,model,0)
        GLES20.glUseProgram(textureProgram)
        GLES20.glUniformMatrix4fv(textureMvpLoc,1,false,mvp,0)
        GLES20.glUniform1i(textureSamplerLoc,0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId)
        GLES20.glUniform3fv(textureTintLoc,1,tint,0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0)
        texturedCube.position(0);GLES20.glEnableVertexAttribArray(texturePositionLoc)
        GLES20.glVertexAttribPointer(texturePositionLoc,3,GLES20.GL_FLOAT,false,20,texturedCube)
        texturedCube.position(3);GLES20.glEnableVertexAttribArray(textureUvLoc)
        GLES20.glVertexAttribPointer(textureUvLoc,2,GLES20.GL_FLOAT,false,20,texturedCube)
        texturedCube.position(0);GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,texturedVertexCount)
        GLES20.glDisableVertexAttribArray(texturePositionLoc);GLES20.glDisableVertexAttribArray(textureUvLoc)
    }
    private fun makeTexturedCube():FloatBuffer{
        val faces=arrayOf(
            floatArrayOf(-.5f,-.5f,.5f, .5f,-.5f,.5f, .5f,.5f,.5f, -.5f,.5f,.5f),
            floatArrayOf(.5f,-.5f,-.5f, -.5f,-.5f,-.5f, -.5f,.5f,-.5f, .5f,.5f,-.5f),
            floatArrayOf(-.5f,-.5f,-.5f, -.5f,-.5f,.5f, -.5f,.5f,.5f, -.5f,.5f,-.5f),
            floatArrayOf(.5f,-.5f,.5f, .5f,-.5f,-.5f, .5f,.5f,-.5f, .5f,.5f,.5f),
            floatArrayOf(-.5f,.5f,.5f, .5f,.5f,.5f, .5f,.5f,-.5f, -.5f,.5f,-.5f),
            floatArrayOf(-.5f,-.5f,-.5f, .5f,-.5f,-.5f, .5f,-.5f,.5f, -.5f,-.5f,.5f)
        )
        val uv=floatArrayOf(0f,1f,1f,1f,1f,0f,0f,0f)
        val order=intArrayOf(0,1,2,0,2,3)
        val out=FloatArray(36*5);var p=0
        for(face in faces)for(vertex in order){
            out[p++]=face[vertex*3];out[p++]=face[vertex*3+1];out[p++]=face[vertex*3+2]
            out[p++]=uv[vertex*2];out[p++]=uv[vertex*2+1]
        }
        texturedVertexCount=out.size/5
        return ByteBuffer.allocateDirect(out.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply{put(out);position(0)}
    }
    private fun brighten(c:FloatArray)=floatArrayOf((c[0]+0.32f).coerceAtMost(1f),(c[1]+0.32f).coerceAtMost(1f),(c[2]+0.32f).coerceAtMost(1f))
    private fun worldTint(c:FloatArray,emissive:Boolean=false):FloatArray{
        if(emissive)return floatArrayOf(c[0].coerceIn(0f,1f),c[1].coerceIn(0f,1f),c[2].coerceIn(0f,1f))
        val brightness=worldLighting.sceneBrightness
        val nightBlue=(1f-worldLighting.daylight)*0.07f
        return floatArrayOf(
            (c[0]*brightness).coerceIn(0f,1f),
            (c[1]*brightness).coerceIn(0f,1f),
            (c[2]*(brightness+nightBlue)).coerceIn(0f,1f)
        )
    }
    private fun drawBox(cx:Float,cy:Float,cz:Float,sx:Float,sy:Float,sz:Float,a:Float,c:FloatArray,emissive:Boolean=false){
        GLES20.glUseProgram(program)
        Matrix.setIdentityM(model,0);Matrix.translateM(model,0,cx,cy,cz);Matrix.rotateM(model,0,Math.toDegrees(a.toDouble()).toFloat(),0f,1f,0f);Matrix.scaleM(model,0,sx,sy,sz);Matrix.multiplyMM(mvp,0,vp,0,model,0)
        GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);cube.position(0);GLES20.glVertexAttribPointer(posLoc,3,GLES20.GL_FLOAT,false,0,cube);GLES20.glEnableVertexAttribArray(posLoc);GLES20.glUniform3fv(colorLoc,1,worldTint(c,emissive),0);GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,vertexCount)
    }
    private fun shader(type:Int,source:String):Int{val s=GLES20.glCreateShader(type);GLES20.glShaderSource(s,source);GLES20.glCompileShader(s);val ok=IntArray(1);GLES20.glGetShaderiv(s,GLES20.GL_COMPILE_STATUS,ok,0);check(ok[0]==GLES20.GL_TRUE){GLES20.glGetShaderInfoLog(s)};return s}
    private fun makeCube():FloatBuffer{val v=floatArrayOf(
        -0.5f,-0.5f,0.5f,0.5f,-0.5f,0.5f,0.5f,0.5f,0.5f,-0.5f,-0.5f,0.5f,0.5f,0.5f,0.5f,-0.5f,0.5f,0.5f,
        0.5f,-0.5f,-0.5f,-0.5f,-0.5f,-0.5f,-0.5f,0.5f,-0.5f,0.5f,-0.5f,-0.5f,-0.5f,0.5f,-0.5f,0.5f,0.5f,-0.5f,
        -0.5f,-0.5f,-0.5f,-0.5f,-0.5f,0.5f,-0.5f,0.5f,0.5f,-0.5f,-0.5f,-0.5f,-0.5f,0.5f,0.5f,-0.5f,0.5f,-0.5f,
        0.5f,-0.5f,0.5f,0.5f,-0.5f,-0.5f,0.5f,0.5f,-0.5f,0.5f,-0.5f,0.5f,0.5f,0.5f,-0.5f,0.5f,0.5f,0.5f,
        -0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,-0.5f,-0.5f,0.5f,0.5f,0.5f,0.5f,-0.5f,-0.5f,0.5f,-0.5f,
        -0.5f,-0.5f,-0.5f,0.5f,-0.5f,-0.5f,0.5f,-0.5f,0.5f,-0.5f,-0.5f,-0.5f,0.5f,-0.5f,0.5f,-0.5f,-0.5f,0.5f)
        vertexCount=v.size/3;return ByteBuffer.allocateDirect(v.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply{put(v);position(0)}}
    private fun rgb(v:Int)=floatArrayOf(((v shr 16)and 255)/255f,((v shr 8)and 255)/255f,(v and 255)/255f)
    private fun rgb(v:Int,alpha:Float):FloatArray{
        val base=rgb(v)
        return floatArrayOf(base[0]*alpha,base[1]*alpha,base[2]*alpha)
    }
    companion object{
        // Tune these values after profiling on real devices; keep gameplay distances independent.
        private const val GARAGE_X=5f
        private const val GARAGE_Z=20f
        private const val GARAGE_RADIUS=7f
        private const val ROAD_SPACING=40f
        private const val SCENE_RENDER_DISTANCE=90f
        private const val VEHICLE_RENDER_DISTANCE=125f
        private const val TEXTURE_CONCRETE=2
        private const val VERTEX="attribute vec4 aPosition; uniform mat4 uMvp; void main(){gl_Position=uMvp*aPosition;}"
        private const val FRAGMENT="precision mediump float; uniform vec3 uColor; void main(){gl_FragColor=vec4(uColor,1.0);}"
        private const val TEXTURE_VERTEX="attribute vec4 aPosition; attribute vec2 aTexCoord; uniform mat4 uMvp; varying vec2 vTexCoord; void main(){gl_Position=uMvp*aPosition;vTexCoord=aTexCoord;}"
        private const val TEXTURE_FRAGMENT="precision mediump float; uniform sampler2D uTexture; uniform vec3 uTint; varying vec2 vTexCoord; void main(){gl_FragColor=texture2D(uTexture,vTexCoord)*vec4(uTint,1.0);}"
    } 
}
