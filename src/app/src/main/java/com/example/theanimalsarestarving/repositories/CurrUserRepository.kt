package com.example.theanimalsarestarving.repositories

import com.example.theanimalsarestarving.models.User

object CurrUserRepository {

    private var currUser: User? = null

    fun getCurrUser(): User? {
        return currUser
    }

    fun setCurrUser(user: User){
        currUser = user
    }


}