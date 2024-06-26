package app.test.xyz

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.test.xyz.databinding.ActivityHomeBinding
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.google.android.material.card.MaterialCardView
import android.graphics.drawable.ColorDrawable
import android.widget.LinearLayout

class HomeActivity : AppCompatActivity(), MaxAdListener {

    private lateinit var interstitialAd: MaxInterstitialAd
    private lateinit var nativeAdLoader: MaxNativeAdLoader
    private var nativeAd: MaxAd? = null

    private val REQUEST_PERMISSION = 123
    private lateinit var binding: ActivityHomeBinding
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var overlayDesignView: View? = null
    private var overlayLockView: View? = null
    private var windowManager: WindowManager? = null
    private var sizeSeekBar: SeekBar? = null
    private val defaultItemSize = 50
    private val defaultOpacity = 225
    private val maxItemSize = 300
    private var opacitySeekBar: SeekBar? = null
    private var languageDialog: androidx.appcompat.app.AlertDialog? = null
    private var selectedColor: Int? = null
    private var isOverlayLocked = false
    var lastClickedCard: MaterialCardView? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = false
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        interstitialAd = MaxInterstitialAd(constant.int_ad, this)
        interstitialAd.setListener(this)

        // Load the first ad
        interstitialAd.loadAd()
        loadNativeAd()


        // Inside onCreate method after initializing views
        val colorViews = listOf<View>(
            findViewById(R.id.color1),
            findViewById(R.id.color2),
            findViewById(R.id.color3),
            findViewById(R.id.color4),
            findViewById(R.id.color5),
            findViewById(R.id.color6),
            findViewById(R.id.color7),
            findViewById(R.id.color8),
            findViewById(R.id.color9),
            findViewById(R.id.color10),
            findViewById(R.id.color11),
            findViewById(R.id.color12)
        )

        var switchList = listOf(
            binding.sw,
            binding.sw2,
            binding.sw3,
            binding.sw4
        ).forEach(::setSwitchTintColor)

        colorViews.forEachIndexed { index, colorView ->
            colorView.setOnClickListener {
                if(binding.sw.isChecked){
                // Get the card background color of the clicked view
                val cardBackgroundColor =
                    (colorView as? MaterialCardView)?.cardBackgroundColor?.defaultColor
                if (cardBackgroundColor != null) {
                    selectedColor = cardBackgroundColor

                    Log.d("ColorClick", "Card background color: $cardBackgroundColor")
                    // Get the ImageView from the overlay layout
                    val aimImageView = overlayDesignView?.findViewById<ImageView>(R.id.aim)
                    // Create a ColorFilter with the desired color and apply it to the overlay image
                    val colorFilter =
                        PorterDuffColorFilter(cardBackgroundColor, PorterDuff.Mode.SRC_ATOP)
                    aimImageView?.colorFilter = colorFilter

                    // Remove stroke from the last clicked card if any
                    lastClickedCard?.strokeWidth = 0

                    // Apply stroke to the clicked card
                    (colorView as? MaterialCardView)?.apply {
                        strokeColor = Color.BLACK // Set stroke color
                        strokeWidth = dpToPx(2) // Set stroke width in dp
                    }

                    // Update the last clicked card
                    lastClickedCard = colorView as? MaterialCardView
                } else {
                    // Log an error message if background color is null
                    Log.e("ColorClick", "Card background color is null")
                }

            }
            }
        }
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        sizeSeekBar = findViewById(R.id.sizebbtn)

        // Set maximum progress of the SeekBar to correspond to maxItemSize
        sizeSeekBar?.max = maxItemSize - defaultItemSize

        // Set initial progress of the SeekBar based on the default size of the overlay item
        sizeSeekBar?.progress = defaultItemSize


        // Set the maximum progress of the SeekBar
        sizeSeekBar?.max = 100

        // Set the initial progress of the SeekBar based on the default image size
        sizeSeekBar?.progress = defaultItemSize

        // Set the SeekBar change listener
        sizeSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                updateOverlayImageSize(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })


        // Initialize SeekBar for opacity
        opacitySeekBar = findViewById(R.id.opasitybtn)
        opacitySeekBar?.max = 255 // Maximum progress for opacity
        opacitySeekBar?.progress = defaultOpacity

// Set the SeekBar change listener for opacity
        opacitySeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                updateOverlayImageOpacity(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.savebtn.isEnabled = false
        binding.sw2.isEnabled = false
        binding.sw3.isEnabled = false
        binding.sw4.isEnabled = false
        binding.sizebbtn.isEnabled = false
        binding.opasitybtn.isEnabled = false
        binding.backgroundbtn.isEnabled = false
        binding.color.isEnabled=false

        binding.back.setOnClickListener {
            finish()
            removeOverlayView()
        }

        binding.sw.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.sw.thumbDrawable.setTint(getResources().getColor(R.color.light_green))
                addOverlayView()
                binding.savebtn.isEnabled = true
                binding.sw2.isEnabled = true
                binding.sw3.isEnabled = true
                binding.sw4.isEnabled = true
                binding.sizebbtn.isEnabled = true
                binding.opasitybtn.isEnabled = true
                binding.sw3.isEnabled = true

            } else {
                binding.sw.thumbDrawable.setTint(getResources().getColor(R.color.text_color_red))
                removeOverlayView()
                binding.savebtn.isEnabled = false
                binding.sw2.isChecked = false
                binding.sw3.isChecked = false
                binding.sw4.isChecked = false
                binding.sw2.isEnabled = false
                binding.sw3.isEnabled = false
                binding.sw4.isEnabled = false
                binding.sizebbtn.isEnabled = false
                binding.opasitybtn.isEnabled = false
                binding.backgroundbtn.isEnabled = false

                binding.sw2.thumbDrawable.setTint(getResources().getColor(R.color.text_color_red))
                binding.sw3.thumbDrawable.setTint(getResources().getColor(R.color.text_color_red))
                binding.sw4.thumbDrawable.setTint(getResources().getColor(R.color.text_color_red))
                lastClickedCard?.strokeWidth = 0

            }
        }

        binding.sw3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.backgroundbtn.isEnabled = true
            }else{
                binding.backgroundbtn.isEnabled = false
            }
        }
        binding.savebtn.setOnClickListener {

            if (binding.savebtn.isEnabled) {
                if (interstitialAd.isReady) {
                    interstitialAd.showAd()
                    // Disable the button after it's clicked
                    binding.savebtn.isEnabled = true
                } else {
                    // If ad is not ready, start activity directly
                    Toast.makeText(this, "Try Again !", Toast.LENGTH_SHORT).show()
                    interstitialAd.loadAd()
                }
            } else {
                Toast.makeText(this, "Please Enable Track Button !", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backgroundbtn.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Ensure a color is selected
                selectedColor?.let { color ->
                    // Adjust opacity based on progress
                    val opacity = (255f * (progress.toFloat() / seekBar!!.max)).toInt()
                    // Combine color and opacity
                    val adjustedColor = Color.argb(opacity, Color.red(color), Color.green(color), Color.blue(color))
                    // Set the background color with adjusted opacity
                    setOverlayImageBackgroundColor(adjustedColor)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setOverlayImageBackgroundColor(color: Int) {
        // Get the ImageView from the overlay layout
        val aimImageView = overlayDesignView?.findViewById<ImageView>(R.id.aim)

        // Set background color of the overlay image
        aimImageView?.setBackgroundColor(color)
    }

    private fun updateOverlayImageSize(progress: Int) {
        // Get the ImageView from the overlay layout
        val aimImageView = overlayDesignView?.findViewById<ImageView>(R.id.aim)
        val aimICardView = overlayDesignView?.findViewById<MaterialCardView>(R.id.aimcard)

        // Convert progress to pixels
        val imageSizePx = dpToPx(progress)

        // Update the size of the overlay image
        aimImageView?.layoutParams?.width = imageSizePx
        aimImageView?.layoutParams?.height = imageSizePx
        aimICardView?.layoutParams?.width = imageSizePx
        aimICardView?.layoutParams?.height = imageSizePx

        // Request layout to reflect the changes
        aimImageView?.requestLayout()
        aimICardView?.requestLayout()
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    private fun addOverlayView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        } else {
            overlayDesignView = LayoutInflater.from(this).inflate(R.layout.overly_design, null)
            overlayLockView = LayoutInflater.from(this).inflate(R.layout.lock_design, null)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.START or Gravity.TOP
            params.x = 40
            params.y = 160

            windowManager?.addView(overlayDesignView, params)
            windowManager?.addView(overlayLockView, params)

            overlayDesignView?.setOnTouchListener { _, event ->
                handleTouch(event)
            }

            overlayLockView?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    // Toggle between locked and unlocked images
                    val imageView = overlayLockView?.findViewById<ImageView>(R.id.iv)
                    if (imageView != null) {
                        val currentDrawable = imageView.drawable
                        val newDrawable = if (currentDrawable.constantState ==
                            resources.getDrawable(R.drawable.unlocked).constantState) {
                            isOverlayLocked = true
                            resources.getDrawable(R.drawable.locked)
                        } else {
                            isOverlayLocked = false
                            resources.getDrawable(R.drawable.unlocked)
                        }
                        imageView.setImageDrawable(newDrawable)
                    }
                }
                true
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleTouch(event: MotionEvent): Boolean {
        if (!isOverlayLocked) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX.toInt()
                    initialY = event.rawY.toInt()
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    val offsetX = event.rawX - initialTouchX
                    val offsetY = event.rawY - initialTouchY
                    val params = overlayDesignView?.layoutParams as WindowManager.LayoutParams
                    params.x = (initialX + offsetX).toInt()
                    params.y = (initialY + offsetY).toInt()
                    windowManager?.updateViewLayout(overlayDesignView, params)
                }
            }
        }
        return true
    }

    private fun removeOverlayView() {
        overlayDesignView?.let {
            windowManager?.removeView(it)
        }
        overlayDesignView = null
        overlayLockView?.let {
            windowManager?.removeView(it)
        }
        overlayLockView = null
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, REQUEST_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                addOverlayView()
            } else {
                Toast.makeText(this, "Overlay permission not granted", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun updateOverlaySize(progress: Int) {
        val params = overlayDesignView?.layoutParams as WindowManager.LayoutParams
        params.width = progress
        params.height = progress
        windowManager?.updateViewLayout(overlayDesignView, params)
    }

    private fun updateOverlayImageOpacity(progress: Int) {
        // Get the ImageView from the overlay layout
        val aimImageView = overlayDesignView?.findViewById<ImageView>(R.id.aim)
        val aimcard = overlayDesignView?.findViewById<MaterialCardView>(R.id.aimcard)

        // Update the alpha value of the overlay image
        aimImageView?.alpha = progress / 255f // Range: 0.0-1.0
        aimcard?.alpha = progress / 255f // Range: 0.0-1.0
    }

    private fun setSwitchTintColor(switchButton: Switch) {
        switchButton.setOnCheckedChangeListener { buttonView, isChecked ->
            // Change the color when the switch is toggled
            if (isChecked) {
                switchButton.thumbDrawable.setTint(getResources().getColor(R.color.light_green))
            } else {
                switchButton.thumbDrawable.setTint(getResources().getColor(R.color.off_switch))
            }
        }
    }

    fun checkPopup() {

        if (!binding.sw.isChecked) {
            showPopup(R.layout.bad_popup)
        } else if (!binding.sw2.isChecked) {
            showPopup(R.layout.bad_popup)

        } else if (!binding.sw3.isChecked) {
            showPopup(R.layout.good_popup)

        } else if (!binding.sw4.isChecked) {
            showPopup(R.layout.good_popup)

        } else {
            showPopup(R.layout.awesome_popup)
        }
    }

    fun showPopup(popup: Int) {
        val customLayout = LayoutInflater.from(this)
            .inflate(popup, null)

        // Create AlertDialog with custom layout
        val builder = AlertDialog.Builder(this, R.style.TransparentDialogTheme)
        builder.setView(customLayout)
        builder.setCancelable(true) // Prevent dismissing AlertDialog by tapping outside or pressing back button

        val popupDialog = builder.create()
        popupDialog.show()
        // Prevent dismissing AlertDialog by tapping outside or pressing back button
        val okaybtn = customLayout.findViewById<LinearLayout>(R.id.okbtn)

        okaybtn.setOnClickListener {
            popupDialog.dismiss()
        }
    }

    override fun onAdLoaded(p0: MaxAd) {
        interstitialAd.isReady
    }

    override fun onAdDisplayed(p0: MaxAd) {

    }

    override fun onAdHidden(p0: MaxAd) {
        // Inflate custom layout for AlertDialog
        val customLayout = LayoutInflater.from(this)
            .inflate(R.layout.custom_alert_layout, null)
        val progressBar: ProgressBar =
            customLayout.findViewById(R.id.progressBar)
        val textView: TextView = customLayout.findViewById(R.id.textView)

        // Create AlertDialog with custom layout
        val builder = AlertDialog.Builder(this)
        builder.setView(customLayout)
        builder.setCancelable(false) // Prevent dismissing AlertDialog by tapping outside or pressing back button

        // Show AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()

        // Simulating some background task delay
        Handler().postDelayed({

            // Dismiss AlertDialog
            alertDialog.dismiss()

            checkPopup()
        }, 2000)
    }

    override fun onAdClicked(p0: MaxAd) {

    }

    override fun onAdLoadFailed(p0: String, p1: MaxError) {
        interstitialAd.loadAd()
    }

    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
        interstitialAd.loadAd()
    }

    private fun loadNativeAd() {
        val nativeAdContainer = findViewById<FrameLayout>(R.id.native_ad_layout)
        nativeAdLoader = MaxNativeAdLoader(constant.nat_id, this)
        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd)
                }
                nativeAd = ad
                nativeAdContainer.removeAllViews()
                nativeAdContainer.addView(nativeAdView)
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                // Handle ad loading failure
            }

            override fun onNativeAdClicked(ad: MaxAd) {
                // Handle ad click
            }
        })
        nativeAdLoader.loadAd()
    }

    override fun onBackPressed() {
        finish()
        removeOverlayView()
    }

    override fun onDestroy() {
        removeOverlayView()
    }
}
