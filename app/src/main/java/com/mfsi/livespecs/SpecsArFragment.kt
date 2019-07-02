package com.mfsi.livespecs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.util.*

/**
 *  Custom ArFragment configured to front camera with face mesh as output.
 *  By default, it is set to rear camera and plane discovery.
 */
class SpecsArFragment : ArFragment(){

    /**
     * This function sets the output to Face Mesh.
     */
    override fun getSessionConfiguration(session: Session?): Config {
        return Config(session).apply { augmentedFaceMode = Config.AugmentedFaceMode.MESH3D }
    }

    /**
     * Configures the session  to use front camera.
     */
    override fun getSessionFeatures(): MutableSet<Session.Feature> {
        return EnumSet.of(Session.Feature.FRONT_CAMERA)
    }

    /**
     * To remove default plane discovery views, we override onCreateView.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val layout = super.onCreateView(inflater, container, savedInstanceState) as FrameLayout
        planeDiscoveryController.apply {
            hide()
            setInstructionView(null)
        }
        return layout
    }
}