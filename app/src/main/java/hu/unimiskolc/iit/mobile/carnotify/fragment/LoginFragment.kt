package hu.unimiskolc.iit.mobile.carnotify.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import hu.unimiskolc.iit.mobile.carnotify.R
import hu.unimiskolc.iit.mobile.carnotify.databinding.LoginFragmentBinding
import hu.unimiskolc.iit.mobile.core.domain.User
import hu.unimiskolc.iit.mobile.framework.db.CarNotifyDatabase
import hu.unimiskolc.iit.mobile.framework.db.datasource.RoomUserDataSource
import hu.unimiskolc.iit.mobile.framework.db.mapper.UserMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import java.util.*




class LoginFragment: Fragment() {

    private var _binding: LoginFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var userDataSource: RoomUserDataSource

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)

        val db = Room.databaseBuilder(
            this.requireContext(),
            CarNotifyDatabase::class.java, "car_notify.db"
        ).build()

        userDataSource = RoomUserDataSource(db.userDao(), UserMapper())

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fingerprintInstructions.visibility =
            if (this.isBiometricReady(this.requireContext())) View.VISIBLE else View.GONE

        binding.fingerprintButton.visibility =
            if (this.isBiometricReady(this.requireContext())) View.VISIBLE else View.GONE

        binding.fingerprintButton.setOnClickListener {
            showBiometricPrompt()
        }

        binding.submitButton.setOnClickListener {
            uiScope.launch {
                val email: String = binding.email.text.toString()
                val password: String = binding.password.text.toString()
                val user = userDataSource.fetchByEmail(email)

                if(user == null) {
                    Toast.makeText(
                        requireContext(),
                        "Invalid email address.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }

                if(BCrypt.checkpw(password, user.password)) {
                    Toast.makeText(
                        requireContext(),
                        "Login successful.",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController()
                        .navigate(
                            R.id.action_LoginFragment_to_MyCarsFragment,
                            bundleOf(Pair("user", user))
                        )

                    return@launch
                }

                Toast.makeText(
                    requireContext(),
                    "Invalid password.",
                    Toast.LENGTH_SHORT
                ).show()

                return@launch
            }
        }

        binding.registerButton.setOnClickListener {
            findNavController()
                .navigate(
                    R.id.action_LoginFragment_to_RegistrationFragment
                )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hasBiometricCapability(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG)
    }

    private fun isBiometricReady(context: Context) =
        hasBiometricCapability(context) == BiometricManager.BIOMETRIC_SUCCESS

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

        // Use Device Credentials if allowed, otherwise show Cancel Button
        builder.apply {
            if (allowDeviceCredential) setAllowedAuthenticators(BIOMETRIC_STRONG)
            else setNegativeButtonText("Cancel")
        }

        return builder.build()
    }

    private fun initBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(requireContext())

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                Toast.makeText(requireContext(),
                    "Authentication error: $errString", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()

                Toast.makeText(requireContext(), "Authentication failed",
                    Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                uiScope.launch {
                    val deviceEmail = "deviceuser@carnotify.com"

                    val user = userDataSource.fetchByEmail(deviceEmail)

                    if(user == null) {
                        Toast.makeText(requireContext(),
                            "Please select your birth date", Toast.LENGTH_SHORT)
                            .show()

                        pickDateTime()

                        return@launch
                    }

                    findNavController()
                        .navigate(
                            R.id.action_LoginFragment_to_MyCarsFragment,
                            bundleOf(Pair("user", user))
                        )

                    Toast.makeText(requireContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        return BiometricPrompt(this, executor, callback)
    }

    private fun showBiometricPrompt(
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

    private fun pickDateTime() {
        val c = Calendar.getInstance()
        val mYear = c[Calendar.YEAR]
        val mMonth = c[Calendar.MONTH]
        val mDay = c[Calendar.DAY_OF_MONTH]

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            {
                    _, year, monthOfYear, dayOfMonth ->
                c.set(year, monthOfYear, dayOfMonth)

                val user = User(
                    0,
                    "Device User",
                    "deviceuser@carnotify.com",
                    BCrypt.hashpw("SeCREtDEViceUSErPAsSworD", BCrypt.gensalt()),
                    c.time,
                    listOf()
                )

                uiScope.launch {
                    userDataSource.add(user)

                    findNavController()
                        .navigate(
                            R.id.action_LoginFragment_to_MyCarsFragment,
                            bundleOf(Pair("user", user))
                        )

                    Toast.makeText(requireContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            mYear,
            mMonth,
            mDay
        )
        datePickerDialog.show()
    }
}