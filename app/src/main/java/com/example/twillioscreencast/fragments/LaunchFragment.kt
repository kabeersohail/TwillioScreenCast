package com.example.twillioscreencast.fragments

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.twillioscreencast.R
import com.example.twillioscreencast.databinding.FragmentLaunchBinding
import com.example.twillioscreencast.managers.ScreenCaptureManager
import com.example.twillioscreencast.utils.TAG
import com.example.twillioscreencast.viewmodels.LaunchViewModel
import com.twilio.video.LocalVideoTrack
import com.twilio.video.ScreenCapturer
import com.twilio.video.VideoView


class LaunchFragment : Fragment() {

    private lateinit var screenCaptureManager: ScreenCaptureManager
    private lateinit var localVideoView: VideoView
    private var screenVideoTrack: LocalVideoTrack? = null
    private var screenCapture: ScreenCapturer? = null

    private val onScreenCaptureResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        Log.d(TAG, "Triggered screenCapturePermissionResult")

        if(activityResult.resultCode == Activity.RESULT_OK){

            activityResult.data?.let { intent ->
                screenCapture = ScreenCapturer(requireContext(), activityResult.resultCode, intent, screenCaptureListener)
                startScreenCapture()
            }
        }
    }

    private val screenCaptureListener: ScreenCapturer.Listener = object : ScreenCapturer.Listener{
        override fun onScreenCaptureError(errorDescription: String) {
            Log.e(TAG, "Screen capture error: $errorDescription")
            stopScreenCapture()
            Toast.makeText(requireContext(), R.string.screen_capture_error, Toast.LENGTH_LONG).show()
        }

        override fun onFirstFrameAvailable() {
            Log.d(TAG, "First frame from screen capture is available")
        }

    }

    private fun stopScreenCapture() {
        if (screenVideoTrack != null) {
            screenVideoTrack?.removeSink(localVideoView)
            screenVideoTrack?.release()
            screenVideoTrack = null
            localVideoView.visibility = View.INVISIBLE
        }
    }

    private lateinit var binding: FragmentLaunchBinding

    private val launchViewModel: LaunchViewModel by lazy {
        ViewModelProvider(this).get(LaunchViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLaunchBinding.inflate(inflater, container, false)
        binding.viewModel = launchViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        localVideoView = binding.localVideo
        screenCaptureManager = ScreenCaptureManager(requireContext())
        startObservation()
    }

    private fun startObservation() {
        launchViewModel.screenShareStatus.observe(viewLifecycleOwner) { screenShareStatus ->
            when (screenShareStatus) {
                true -> {
                    Log.d(TAG, "Requesting permission to capture screen")

                    if(Build.VERSION.SDK_INT >= 29) screenCaptureManager.startForeground()

                    val mediaProjectionManager: MediaProjectionManager =
                        requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

                    if(screenCapture == null) requestScreenCapturePermission(mediaProjectionManager) else startScreenCapture()

                }
                false -> {
                    if (Build.VERSION.SDK_INT >= 29) screenCaptureManager.endForeground()
                    stopScreenCapture()
                    Toast.makeText(requireContext(), "Screen sharing stopped", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startScreenCapture() {

        screenVideoTrack = LocalVideoTrack.create(
            requireContext(),
            true,
            screenCapture ?: kotlin.run {
            Log.e(TAG,"Screen capture is null")
            return
        })

        localVideoView.mirror = false

        localVideoView.visibility = View.VISIBLE
        screenVideoTrack?.addSink(localVideoView)

    }

    private fun requestScreenCapturePermission(mediaProjectionManager: MediaProjectionManager) =
        onScreenCaptureResult.launch(mediaProjectionManager.createScreenCaptureIntent())

     override fun onDestroy() {
        if (screenVideoTrack != null) {
            screenVideoTrack?.release()
            screenVideoTrack = null
        }
        if (Build.VERSION.SDK_INT >= 29) {
            screenCaptureManager.unbindService()
        }
        super.onDestroy()
    }

}