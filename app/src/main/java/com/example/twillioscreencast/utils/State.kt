package com.example.twillioscreencast.utils

sealed class State{
    object BindService : State()
    object StartForeground: State()
    object EndForeground: State()
    object UnbindService: State()
}
