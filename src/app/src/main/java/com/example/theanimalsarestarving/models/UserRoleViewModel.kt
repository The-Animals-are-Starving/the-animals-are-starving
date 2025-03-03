package com.example.theanimalsarestarving.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// ViewModel to hold the user's role
// THIS IS ONLY FOR THE UI IN MAIN ACTIVITY
class UserRoleViewModel : ViewModel() {

    // LiveData to observe changes in the user role
    private val _userRole = MutableLiveData<UserRole>()
    val userRole: LiveData<UserRole> get() = _userRole

    // Function to update the CURRENT??? user role
    fun setUserRole(role: UserRole) {
        _userRole.value = role
    }
}