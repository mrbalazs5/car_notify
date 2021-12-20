package hu.unimiskolc.iit.mobile.carnotify

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class BiometricAuthenticator(
    private val fragment: Fragment,
    private val onAuthenticationSucceededFun: () -> Unit
) {
    companion object {
        private fun hasBiometricCapability(context: Context): Int {
            val biometricManager = BiometricManager.from(context)
            return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        }

        fun isBiometricReady(context: Context) =
            hasBiometricCapability(context) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun initBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(fragment.requireContext())

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                Toast.makeText(
                    fragment.requireContext(),
                "Authentication error: $errString", Toast.LENGTH_SHORT
                )
                .show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()

                Toast.makeText(fragment.requireContext(), "Authentication failed",
                    Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                onAuthenticationSucceededFun()
            }
        }

        return BiometricPrompt(fragment, executor, callback)
    }

    fun showBiometricPrompt(
        title: String = "Biometric Authentication",
        subtitle: String = "Enter biometric credentials to proceed.",
        description: String = "Input your Fingerprint or FaceID to ensure it's you!",
        cryptoObject: BiometricPrompt.CryptoObject? = null,
        allowDeviceCredential: Boolean = false
    ) {
        val promptInfo = setBiometricPromptInfo(
            title,
            subtitle,
            description,
            allowDeviceCredential
        )

        val biometricPrompt = initBiometricPrompt()

        biometricPrompt.apply {
            if (cryptoObject == null) authenticate(promptInfo)
            else authenticate(promptInfo, cryptoObject)
        }
    }

    private fun setBiometricPromptInfo(
        title: String,
        subtitle: String,
        description: String,
        allowDeviceCredential: Boolean
    ): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)

        builder.apply {
            if (allowDeviceCredential) setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            else setNegativeButtonText("Cancel")
        }

        return builder.build()
    }
}