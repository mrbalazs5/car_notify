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
import hu.unimiskolc.iit.mobile.carnotify.R
import hu.unimiskolc.iit.mobile.carnotify.databinding.UserSettingsBinding
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

class UserSettingsFragment: Fragment() {
    private var _binding: UserSettingsBinding? = null

    private val binding get() = _binding!!

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var userDataSource: RoomUserDataSource

    private var birthDateCalendar: Calendar = Calendar.getInstance()

    private var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UserSettingsBinding.inflate(inflater, container, false)

        val db = Room.databaseBuilder(
            this.requireContext(),
            CarNotifyDatabase::class.java, "car_notify.db"
        ).build()

        userDataSource = RoomUserDataSource(db.userDao(), UserMapper())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.US)

        val userId = arguments?.get("userId") as Int

        val deviceEmail = getString(R.string.device_email)

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                birthDateCalendar.set(Calendar.YEAR, year)
                birthDateCalendar.set(Calendar.MONTH, monthOfYear)
                birthDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.birthDateValue.text = formatter.format(birthDateCalendar.time)
            }

        binding.birthDate.setOnClickListener {
            DatePickerDialog(
                this.requireContext(),
                dateSetListener,
                birthDateCalendar.get(Calendar.YEAR),
                birthDateCalendar.get(Calendar.MONTH),
                birthDateCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        uiScope.launch {
            user = userDataSource.fetchById(userId)

            if(user?.email == deviceEmail) {
                binding.email.visibility = View.GONE
                binding.password.visibility = View.GONE
            }

            binding.name.setText(user?.name)
            binding.email.setText(user?.email)
            binding.birthDateValue.text = formatter.format(user!!.birthDate)
            birthDateCalendar.time = user!!.birthDate
        }

        binding.saveButton.setOnClickListener {
            uiScope.launch {
                val name: String = binding.name.text.toString()
                val email: String = binding.email.text.toString()
                val password: String = binding.password.text.toString()

                if(user?.email != email) {
                    val existingUser = userDataSource.fetchByEmail(email)

                    if(existingUser != null || email == deviceEmail) {
                        Toast.makeText(
                            requireContext(),
                            "Email address already taken.",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@launch
                    }
                }

                user?.name = if(user?.name != name && name != "") name else user?.name!!
                user?.email = if(user?.email != email && email != "") email else user?.email!!
                user?.password = if(user?.password != password && password != "") BCrypt.hashpw(password, BCrypt.gensalt()) else user?.password!!
                user?.birthDate = birthDateCalendar.time

                userDataSource.update(user!!)

                Toast.makeText(
                    requireContext(),
                    "Your changes has been saved.",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController()
                    .navigate(
                        R.id.action_UserSettingsFragment_to_MyCarsFragment,
                        bundleOf(Pair("user", user))
                    )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}