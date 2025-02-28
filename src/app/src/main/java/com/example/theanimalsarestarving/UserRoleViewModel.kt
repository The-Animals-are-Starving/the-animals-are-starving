import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.theanimalsarestarving.UserRole

// ViewModel to hold the user's role
class UserRoleViewModel : ViewModel() {

    // LiveData to observe changes in the user role
    private val _userRole = MutableLiveData<UserRole>()
    val userRole: LiveData<UserRole> get() = _userRole

    // Function to update the user role
    fun setUserRole(role: UserRole) {
        _userRole.value = role
    }
}