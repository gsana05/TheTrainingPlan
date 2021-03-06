package com.thetrainingplan.models

class Goal(
    var id : String?,
    val userId : String?,
    val goalSetDate : Long?,
    val goal : String?,
    val goalType : Int?,
    val goalDateDeadline : Long?,
    val isDeleted : Long?,
    val isCompleted : Long?,
    val isPermanentlyDeleted : Boolean?
)