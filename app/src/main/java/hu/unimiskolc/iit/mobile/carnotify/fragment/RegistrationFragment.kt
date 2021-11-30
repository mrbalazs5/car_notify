package hu.unimiskolc.iit.mobile.carnotify.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
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

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submitButton.setOnClickListener {
            uiScope.launch {
                val parser =  SimpleDateFormat("yyyy-MM-dd")

                val name: String = binding.name.text.toString()
                val email: String = binding.email.text.toString()
                val password: String = binding.password.text.toString()
                val birthDateString: String = binding.birthDate.text.toString()

                if(name == "" || email == "" || password == "" || birthDateString == "") {
                    Toast.makeText(
                        requireContext(),
                        "Missing form data.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }

                val formattedBirthDate: Date = parser.parse(birthDateString)!!

                val user = User(
                    0,
                    name,
                    email,
                    BCrypt.hashpw(password, BCrypt.gensalt()),
                    formattedBirthDate,
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

                return@launch
            }
        }
    }
}