package com.eztier.testbadsqlmodel.domain

sealed trait ValidationError extends Product with Serializable

case object TrialNotFoundError extends ValidationError
case object TrialArmNotFoundError extends ValidationError
case object TrialContractNotFoundError extends ValidationError
