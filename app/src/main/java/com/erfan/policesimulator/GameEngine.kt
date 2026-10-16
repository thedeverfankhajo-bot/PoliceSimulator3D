package com.erfan.policesimulator

import android.content.Context
import android.opengl.GLES20
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
import kotlin.math.sin
import kotlin.math.sqrt

data class GameSnapshot(val speed: Int, val lights: Boolean, val siren: Boolean, val onFoot: Boolean, val mission: String, val objective: String, val distance: Int, val score: Int, val radio: String)

class GameEngine(context: Context) : GLSurfaceView.Renderer {
    @Volatile private var gas=false
    @Volatile private var brake=false
    @Volatile private var steering=0f
    @Volatile private var interact=false
    @Volatile private var nextCall=false
    @Volatile private var vehicleToggle=false
    @Volatile private var sirenToggle=false
    @Volatile private var x=0f
    @Volatile private var z=12f
    @Volatile private var angle=0f
    @Volatile private var speed=0f
    @Volatile private var lights=false
    @Volatile private var siren=false
    @Volatile private var onFoot=false
    @Volatile private var stage=0
    @Volatile private var score=0
    @Volatile private var suspectX=2.5f
    @Volatile private var suspectZ=-38f
    @Volatile private var warrant=true
    @Volatile private var radio="Dispatch: Unit 12 ready for patrol."
    @Volatile private var objective="Accept a call, activate lights and siren, then approach the suspect."
    @Volatile private var message="Welcome, Officer. Accept the dispatch call."
    @Volatile private var messageUntil=SystemClock.uptimeMillis()+4000L
    @Volatile private var listener: ((GameSnapshot,String)->Unit)?=null
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
    private var viewportWidth=1
    private var viewportHeight=1
    private data class Box(val x:Float,val y:Float,val z:Float,val sx:Float,val sy:Float,val sz:Float,val color:FloatArray)
    private data class Car(var x:Float,var z:Float,val angle:Float,val speed:Float,val color:FloatArray,val verticalRoad:Boolean)

    fun setListener(value:(GameSnapshot,String)->Unit){listener=value}
    fun gas(value:Boolean){gas=value}
    fun brake(value:Boolean){brake=value}
    fun steer(value:Float){steering=value.coerceIn(-1f,1f)}
    fun interact(){interact=true}
    fun requestCall(){nextCall=true}
    fun toggleVehicle(){vehicleToggle=true}
    fun toggleSiren(){sirenToggle=true}
    fun snapshot()=GameSnapshot((abs(speed)*3.6f).toInt(),lights,siren,onFoot,missionName(),objective,distance(x,z,suspectX,suspectZ).toInt(),score,radio)

    override fun onSurfaceCreated(gl:GL10?,config:EGLConfig?){
        GLES20.glClearColor(0.43f,0.66f,0.79f,1f);GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        val vs=shader(GLES20.GL_VERTEX_SHADER,VERTEX);val fs=shader(GLES20.GL_FRAGMENT_SHADER,FRAGMENT)
        program=GLES20.glCreateProgram();GLES20.glAttachShader(program,vs);GLES20.glAttachShader(program,fs);GLES20.glLinkProgram(program)
        val status=IntArray(1);GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,status,0)
        check(status[0]==GLES20.GL_TRUE){GLES20.glGetProgramInfoLog(program)}
        posLoc=GLES20.glGetAttribLocation(program,"aPosition");colorLoc=GLES20.glGetUniformLocation(program,"uColor");mvpLoc=GLES20.glGetUniformLocation(program,"uMvp")
        cube=makeCube();buildCity();buildTraffic()
    }
    override fun onSurfaceChanged(gl:GL10?,width:Int,height:Int){
        viewportWidth=max(1,width);viewportHeight=max(1,height);GLES20.glViewport(0,0,viewportWidth,viewportHeight)
        Matrix.perspectiveM(projection,0,58f,viewportWidth.toFloat()/viewportHeight,0.1f,500f)
    }
    override fun onDrawFrame(gl:GL10?){
        val now=SystemClock.uptimeMillis();val dt=if(lastFrame==0L)0.016f else ((now-lastFrame).coerceAtMost(40L)/1000f);lastFrame=now
        update(dt,now);GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        val back=if(onFoot)5.5f else 9.5f
        Matrix.setLookAtM(view,0,x+sin(angle)*back,if(onFoot)3f else 5f,z+cos(angle)*back,x-sin(angle)*2f,if(onFoot)1f else 0.8f,z-cos(angle)*2f,0f,1f,0f)
        Matrix.multiplyMM(vp,0,projection,0,view,0);GLES20.glUseProgram(program)
        for(b in boxes)drawBox(b.x,b.y,b.z,b.sx,b.sy,b.sz,0f,b.color)
        for(c in traffic)drawCar(c.x,c.z,c.angle,c.color,false,now)
        if(stage!=5)drawCar(suspectX,suspectZ,0f,rgb(0x9aa5ab),false,now)
        if(!onFoot)drawCar(patrolX,patrolZ,angle,rgb(0x1d4777),true,now) else drawOfficer()
        if(now-lastUi>180L){lastUi=now;listener?.invoke(snapshot(),if(now<messageUntil)message else "")}
    }
    private fun update(dt:Float,now:Long){
        if(sirenToggle){sirenToggle=false;siren=!siren;lights=siren;radio=if(siren)"Dispatch: emergency equipment activated." else "Dispatch: emergency equipment off."}
        if(vehicleToggle){vehicleToggle=false
            if(!onFoot&&abs(speed)<1.5f){onFoot=true;speed=0f;x+=cos(angle)*1.7f;z-=sin(angle)*1.7f;announce("Officer on foot.",now)}
            else if(onFoot&&distance(x,z,patrolX,patrolZ)<5f){onFoot=false;x=patrolX;z=patrolZ;announce("Back in patrol vehicle.",now)}
            else announce(if(onFoot)"Move closer to your patrol car." else "Stop before exiting.",now)
        }
        if(nextCall){nextCall=false
            if(stage==0||stage==5){stage=1;suspectX=(kotlin.math.round(x/40f)*40f)+2.5f;suspectZ=(kotlin.math.round((z-65f)/40f)*40f)+2f
                warrant=SystemClock.uptimeMillis()%2L==0L;objective="Activate lights and siren, then approach the highlighted vehicle."
                radio="Dispatch: suspicious vehicle assigned to Unit 12.";announce("Call accepted. Locate the suspect.",now)}
            else announce("Complete the current call first.",now)
        }
        if(!onFoot){if(gas)speed+=12f*dt else if(brake)speed-=19f*dt else speed*=max(0f,1f-0.8f*dt)
            speed=speed.coerceIn(-6f,30f);angle+=steering*dt*(0.3f+min(abs(speed)/12f,1f))*if(speed>=0f)1f else -1f
            x+=-sin(angle)*speed*dt;z+=-cos(angle)*speed*dt;x=x.coerceIn(-155f,155f);z=z.coerceIn(-155f,155f);patrolX=x;patrolZ=z
        }else{angle+=steering*dt*1.8f;val move=if(gas)1f else if(brake)-1f else 0f;x+=-sin(angle)*move*4f*dt;z+=-cos(angle)*move*4f*dt}
        traffic.forEach{c->if(c.verticalRoad){c.z+=-cos(c.angle)*c.speed*dt;if(c.z< -165f)c.z=165f;if(c.z>165f)c.z= -165f}else{c.x+=-sin(c.angle)*c.speed*dt;if(c.x< -165f)c.x=165f;if(c.x>165f)c.x= -165f}}
        if(stage==1){if(lights&&siren&&distance(x,z,suspectX,suspectZ)<22f){stage=2;objective="Stop safely, exit the car and approach the driver.";radio="Dispatch: suspect stopped. Conduct a field contact.";announce("Vehicle stopped. Conduct a field contact.",now)}
            else{suspectZ-=5f*dt;if(suspectZ< -165f)suspectZ=165f}}
        if(interact){interact=false;handleInteraction(now)}
    }
    private fun handleInteraction(now:Long){when(stage){
        0->announce("Accept a dispatch call first.",now)
        1->announce("Activate lights and siren and approach the suspect.",now)
        2->if(onFoot&&distance(x,z,suspectX,suspectZ)<6f){stage=3;objective="Interact again to request licence and warrant records.";radio="Dispatch: field contact started.";announce("Driver contact started.",now)}else announce("Exit the vehicle and move closer.",now)
        3->{stage=4;objective=if(warrant)"Active warrant confirmed. Interact to arrest the driver." else "No active warrant. Interact to issue a citation or warning.";radio=if(warrant)"Dispatch: active warrant confirmed." else "Dispatch: no active warrant.";announce(if(warrant)"Warrant confirmed." else "No active warrant found.",now)}
        4->{score+=if(warrant)250 else 120;stage=5;lights=false;siren=false;objective="Call complete. Request another dispatch assignment.";radio="Dispatch: incident cleared. Unit 12 available.";announce("Call complete. XP awarded.",now)}
        else->announce("Request the next call.",now)
    }}
    private fun missionName()=when(stage){0->"Dispatch available";1->"En route: vehicle stop";2->"Suspect stopped";3->"Field contact";4->"Records checked";else->"Call complete"}
    private fun announce(text:String,now:Long){message=text;messageUntil=now+2800L}
    private fun distance(ax:Float,az:Float,bx:Float,bz:Float):Float{val dx=ax-bx;val dz=az-bz;return kotlin.math.sqrt(dx*dx+dz*dz)}
    private fun buildCity(){
        boxes.clear();boxes.add(Box(0f,-0.3f,0f,360f,0.5f,360f,rgb(0x626a63)));val roads=(-3..3).map{it*40f}
        roads.forEach{rx->boxes.add(Box(rx,0f,0f,12f,0.08f,340f,rgb(0x30383e)));boxes.add(Box(rx-6.2f,0.08f,0f,0.8f,0.16f,340f,rgb(0x929898)));boxes.add(Box(rx+6.2f,0.08f,0f,0.8f,0.16f,340f,rgb(0x929898)));for(rz in -160..160 step 20)boxes.add(Box(rx,0.06f,rz.toFloat(),0.13f,0.02f,4f,rgb(0xe8c967)))}
        roads.forEach{rz->boxes.add(Box(0f,0f,rz,340f,0.08f,12f,rgb(0x30383e)));boxes.add(Box(0f,0.08f,rz-6.2f,340f,0.16f,0.8f,rgb(0x929898)));boxes.add(Box(0f,0.08f,rz+6.2f,340f,0.16f,0.8f,rgb(0x929898)))}
        val colors=intArrayOf(0x626e79,0x737e83,0x817b71,0x586a76,0x777f76);var i=0
        for(gx in -100..100 step 40)for(gz in -100..100 step 40){val bx=gx+(i%7-3)*1.1f;val bz=gz+(i%5-2)*1.2f;val w=10f+(i%4)*1.7f;val d=10f+(i%3)*2f;val h=9f+(i%6)*3.2f
            boxes.add(Box(bx,h/2f,bz,w,h,d,rgb(colors[i++%colors.size])));boxes.add(Box(bx,h+0.1f,bz,w+0.2f,0.2f,d+0.2f,rgb(0x3f4c55)))
            for(row in 0..3)for(col in -1..1){val wy=1.6f+row*(h-2f)/4f;boxes.add(Box(bx+col*w*0.27f,wy,bz-d/2f-0.03f,w*0.11f,0.72f,0.05f,rgb(0x8bb6c1)));boxes.add(Box(bx+col*w*0.27f,wy,bz+d/2f+0.03f,w*0.11f,0.72f,0.05f,rgb(0x8bb6c1)))}
        }
        for(lx in -120..120 step 80)for(lz in -120..120 step 80){boxes.add(Box(lx+8f,2.1f,lz+8f,0.15f,4.2f,0.15f,rgb(0x4b565e)));boxes.add(Box(lx+8f,4.15f,lz+7.4f,0.2f,0.14f,1.2f,rgb(0xded5a7)))}
    }
    private fun buildTraffic(){traffic.clear();repeat(10){i->val vertical=i%2==0;val lane=((i%7)-3)*40f+if(i%3==0)2.5f else -2.5f;val a=if(vertical){if(i%4==0)0f else Math.PI.toFloat()}else{if(i%4==0)(Math.PI/2).toFloat()else(-Math.PI/2).toFloat()};val c=intArrayOf(0xcbd1d3,0x9aa8b0,0x718891,0xb5a68e)[i%4];traffic.add(if(vertical)Car(lane,(i*29-130).toFloat(),a,4f+(i%4),rgb(c),true)else Car((i*31-140).toFloat(),lane,a,4f+(i%4),rgb(c),false))}}
    private fun drawOfficer(){drawBox(x,1f,z,0.52f,1f,0.38f,angle,rgb(0x17365c));drawBox(x,1.65f,z,0.36f,0.36f,0.34f,angle,rgb(0xd1a17e));drawBox(x,1.86f,z,0.39f,0.12f,0.38f,angle,rgb(0x152b45))}
    private fun drawCar(cx:Float,cz:Float,a:Float,paint:FloatArray,police:Boolean,now:Long){
        drawBox(cx,0.52f,cz,1.8f,0.5f,3.55f,a,paint);drawBox(cx,0.91f,cz,1.18f,0.48f,1.75f,a,if(police)rgb(0xe1e7e9)else rgb(0xbac5c8))
        for(side in intArrayOf(-1,1))for(end in intArrayOf(-1,1)){val lx=side*0.92f;val lz=end*1.05f;drawBox(cx+lx*cos(a)+lz*sin(a),0.32f,cz-lx*sin(a)+lz*cos(a),0.28f,0.48f,0.58f,a,rgb(0x171c21))}
        if(police){drawBox(cx,1.2f,cz,0.8f,0.12f,0.28f,a,rgb(0x17202a));val flash=now/230L%2L==0L;drawBox(cx-0.23f,1.3f,cz,0.32f,0.14f,0.22f,a,rgb(if(lights&&flash)0xff394b else 0x7d202a));drawBox(cx+0.23f,1.3f,cz,0.32f,0.14f,0.22f,a,rgb(if(lights&&!flash)0x235dff else 0x20356d))}
    }
    private fun drawBox(cx:Float,cy:Float,cz:Float,sx:Float,sy:Float,sz:Float,a:Float,c:FloatArray){
        Matrix.setIdentityM(model,0);Matrix.translateM(model,0,cx,cy,cz);Matrix.rotateM(model,0,Math.toDegrees(a.toDouble()).toFloat(),0f,1f,0f);Matrix.scaleM(model,0,sx,sy,sz);Matrix.multiplyMM(mvp,0,vp,0,model,0)
        GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);cube.position(0);GLES20.glVertexAttribPointer(posLoc,3,GLES20.GL_FLOAT,false,0,cube);GLES20.glEnableVertexAttribArray(posLoc);GLES20.glUniform3fv(colorLoc,1,c,0);GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,vertexCount)
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
    companion object{private const val VERTEX="attribute vec4 aPosition; uniform mat4 uMvp; void main(){gl_Position=uMvp*aPosition;}";private const val FRAGMENT="precision mediump float; uniform vec3 uColor; void main(){gl_FragColor=vec4(uColor,1.0);}"} 
}
