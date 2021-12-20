package hu.unimiskolc.iit.mobile.carnotify.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import hu.unimiskolc.iit.mobile.carnotify.R
import hu.unimiskolc.iit.mobile.carnotify.databinding.RegistrationFragmentBinding
import hu.unimiskolc.iit.mobile.core.domain.User
import hu.unimiskolc.iit.mobile.framework.db.CarNotifyDatabase
import hu.unimiskolc.iit.mobile.framework.db.datasource.RoomUserDataSource
import hu.unimiskolc.iit.mobile.framework.db.mapper.UserMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import java.text.SimpleDateFormat
import java.util.*

class RegistrationFragment: Fragment() {
    private var _binding: RegistrationFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var userDataSource: RoomUserDataSource

    private var birthDateCalendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RegistrationFragmentBinding.inflate(inflater, container, false)

        val db = Room.databaseBuilder(
            this.requireContext(),
            CarNotifyDatabase::class.java, "car_notify.db"
        ).build()

        userDataSource = RoomUserDataSource(db.userDao(), UserMapper())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.US)

            birthDateCalendar.set(Calendar.YEAR, year)
            birthDateCalendar.set(Calendar.MONTH, monthOfYear)
            birthDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            binding.birthDateValue.text = formatter.format(birthDateCalendar.time)
        }

        binding.birthDate.setOnClickListener {
            DatePickerDialog(this.requireContext(),
                dateSetListener,
                birthDateCalendar.get(Calendar.YEAR),
                birthDateCalendar.get(Calendar.MONTH),
                birthDateCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.submitButton.setOnClickListener {
            uiScope.launch {
                val name: String = binding.name.text.toString()
                val email: String = binding.email.text.toString()
                val password: String = binding.password.text.toString()

                if(name == "" || email == "" || password == "") {
                    Toast.makeText(
                        requireContext(),
                        "Missing form data.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }

                val existingUser = userDataSource.fetchByEmail(email)
                val deviceEmail = getString(R.string.device_email)

                if(existingUser != null || email == deviceEmail) {
                    Toast.makeText(
                        requireContext(),
                        "Email address already taken.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }

                val user = User(
                    0,
                    name,
                    email,
                    BCrypt.hashpw(password, BCrypt.gensalt()),
                    birthDateCalendar.time,
                    listOf()
                )

                userDataSource.add(user)

                Toast.makeText(
                    requireContext(),
                    "Registration successful.",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController()
                    .navigate(
                        R.id.action_RegistrationFragment_to_LoginFragment
                    )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}