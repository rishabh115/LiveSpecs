package com.mfsi.livespecs

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.AugmentedFaceNode

class SpecsActivity : AppCompatActivity() {

    private lateinit var arFragment: SpecsArFragment
    private lateinit var modelRenderable: ModelRenderable
    private val faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()

    /**
     * Before performing any AR activity, device needs to be verified.
     * After that model has to loaded and mapped to the augmented face nodes returned.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verifyCompatibility()
        setContentView(R.layout.activity_specs)
        arFragment = supportFragmentManager.findFragmentById(R.id.specsar_fragment) as SpecsArFragment
        loadModel()
        placeAndAlignSpecs()
    }

    /**
     * This method gets the scene object from arSceneView.
     * An update listener is added to scene so that on each update the
     * 3d object is properly rendered. All the augmented face trackables are
     * acted upon properly.
     */
    private fun placeAndAlignSpecs(){
        val sceneView = arFragment.arSceneView
        sceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST

        val scene = sceneView.scene

        scene.addOnUpdateListener { _ ->

            val collection: Collection<AugmentedFace>? = sceneView.session?.getAllTrackables(AugmentedFace::class.java)
            collection?.forEach{ face ->
                if (!faceNodeMap.containsKey(face)) {
                    val faceNode = AugmentedFaceNode(face)
                    faceNode.apply {
                        setParent(scene)
                        faceRegionsRenderable = modelRenderable
                    }
                    faceNodeMap[face] = faceNode
                }
            }

            val iterator = faceNodeMap.entries.iterator()

            while (iterator.hasNext()){
                val entry = iterator.next()
                val face = entry.key

                // Removes the  faceNode if the tracking is stopped or face goes out of the frame.
                if (face.trackingState == TrackingState.STOPPED) {
                    val faceNode = entry.value
                    faceNode.setParent(null)
                    iterator.remove()
                }
            }
        }
    }

    private fun verifyCompatibility() {
        if (!isSupportedDeviceOrFinish(this))
            return
    }

    /**
     * Checks if the device is compatible for AR Core.
     */
    private fun isSupportedDeviceOrFinish(specsActivity: SpecsActivity): Boolean {
        if (ArCoreApk.getInstance().checkAvailability(specsActivity) == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE){
           toast(text = "This app requires Ar Core")
           specsActivity.finish()
           return false
        }

        if (getOpenGlVersion(specsActivity)< MIN_OPENGL_VERSION){
            toast(text = "Min supported OpenGL not available!")
            specsActivity.finish()
            return false
        }

        return true
    }

    /**
     * Loads the 3D model and tweaks it to neither cast nor receive shadow.
     */
    private fun loadModel(){
        ModelRenderable.builder()
            .setSource(this, Uri.parse("glasses.sfb"))
            .build()
            .thenAccept{ model ->
                model.apply {
                   isShadowCaster = false
                   isShadowReceiver = false
            }
            modelRenderable = model
        }
    }

    /**
     * This method returns the OpenGl version.
     */
    private fun getOpenGlVersion(activity: AppCompatActivity): Double{
      val config = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
      return config.deviceConfigurationInfo.glEsVersion.toDouble()
    }

    companion object{
        const val MIN_OPENGL_VERSION = 3.0
    }
}
