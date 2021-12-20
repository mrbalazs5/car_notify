package hu.unimiskolc.iit.mobile.carnotify.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import hu.unimiskolc.iit.mobile.carnotify.BiometricAuthenticator
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
            if (BiometricAuthenticator.isBiometricReady(this.requireContext())) View.VISIBLE else View.GONE

        binding.fingerprintButton.visibility =
            if (BiometricAuthenticator.isBiometricReady(this.requireContext())) View.VISIBLE else View.GONE

        binding.fingerprintButton.setOnClickListener {
            val biometricAuthenticator = BiometricAuthenticator(this) {
                uiScope.launch {
                    val deviceEmail = getString(R.string.device_email)

                    val user = userDataSource.fetchByEmail(deviceEmail)

                    if (user == null) {
                        Toast.makeText(
                            requireContext(),
                            "Please select your birth date", Toast.LENGTH_SHORT
                        )
                            .show()

                        pickDateTime()

                        return@launch
                    }

                    findNavController()
                        .navigate(
                            R.id.action_LoginFragment_to_MyCarsFragment,
                            bundleOf(Pair("user", user))
                        )

                    Toast.makeText(
                        requireContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            biometricAuthenticator.showBiometricPrompt()
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
                        "Authentication succeeded!",
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

                val deviceEmail = getString(R.string.device_email)

                val user = User(
                    0,
                    "Device User",
                    deviceEmail,
                    BCrypt.hashpw("SeCREtDEViceUSErPAsSworD", BCrypt.gensalt()),
                    c.time,
                    listOf()
                )

                uiScope.launch {
                    userDataSource.add(user)

                    val dbUser = userDataSource.fetchByEmail(deviceEmail)

                    findNavController()
                        .navigate(
                            R.id.action_LoginFragment_to_MyCarsFragment,
                            bundleOf(Pair("user", dbUser))
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