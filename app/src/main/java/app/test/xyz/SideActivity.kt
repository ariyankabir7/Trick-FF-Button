package app.test.xyz

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import app.test.xyz.databinding.ActivitySideBinding
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import java.util.Locale

class SideActivity : AppCompatActivity() {
    lateinit var binding: ActivitySideBinding
    private var languageDialog: AlertDialog? = null
    private lateinit var nativeAdLoader: MaxNativeAdLoader
    private var nativeAd: MaxAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySideBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val insetsController = ViewCompat.getWindowInsetsController(v)
            insetsController?.isAppearanceLightStatusBars = false
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

// Check if language is set before showing the dialog


        if (!isLanguageSet()) {
            showLanguagePopupDialog()
        }

        loadNativeAd()

        binding.c1.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        binding.c2.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
        binding.c3.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val appLink = "https://play.google.com/store/apps/details?id=$packageName"
            val shareMessage =
                "This is Sensi Tool App :\n$appLink"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Share app via"))
        }
        binding.c4.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com")
                )
            )
        }
    }

    private fun showPpopupDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this, R.style.TransparentDialogTheme)
            .setView(R.layout.back_popup)
            .setCancelable(true).create().apply {
                show()

                findViewById<MaterialButton>(R.id.buttonCancel)?.setOnClickListener {
                    dismiss()
                }
                findViewById<MaterialButton>(R.id.buttonConfirm)?.setOnClickListener {
                    dismiss()
                    super.onBackPressed()
                    finish()
                }
            }

    }

    private fun isLanguageSet(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPreferences.getBoolean("language_set", false)

    }

    @SuppressLint("MissingInflatedId")
    private fun showLanguagePopupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.language_selection_dialog, binding.root, false)

        // Create the dialog
        languageDialog = AlertDialog.Builder(this, R.style.TransparentDialogTheme)
            .setView(dialogView)
            .setCancelable(false) // Set dialog as not cancelable to avoid window leaks
            .create()

        // Show the dialog
        languageDialog?.show()

        dialogView.findViewById<LinearLayout>(R.id.eng)?.setOnClickListener {
            setLocale("en")
            languageDialog?.dismiss()
        }

        languageDialog?.setOnDismissListener {
            languageDialog = null // Clear the reference to the dialog
        }

        dialogView.findViewById<LinearLayout>(R.id.spn)?.setOnClickListener {
            setLocale("es")
            languageDialog?.dismiss()
        }

        dialogView.findViewById<LinearLayout>(R.id.arb)?.setOnClickListener {
            setLocale("ar")
            languageDialog?.dismiss()
        }

        dialogView.findViewById<LinearLayout>(R.id.por)?.setOnClickListener {
            setLocale("pt")
            languageDialog?.dismiss()
        }
        dialogView.findViewById<LinearLayout>(R.id.vit)?.setOnClickListener {
            setLocale("vi")
            languageDialog?.dismiss()
        }
        dialogView.findViewById<LinearLayout>(R.id.thai)?.setOnClickListener {
            setLocale("th")
            languageDialog?.dismiss()
        }
    }

    private fun setLocale(languageCode: String) {

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = getResources()
        val configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)

        // Store a flag indicating that the language has been set
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit().putBoolean("language_set", true).apply()
        recreate()

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
        showPpopupDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Dismiss the dialog if it's shown and the activity is being destroyed
        languageDialog?.dismiss()
    }

}