package com.eztier.testbadsqlmodel
package infrastructure.repository.doobie

import cats.Applicative
import cats.data.OptionT
import cats.effect.Bracket
import doobie._
import doobie.implicits._
import domain.trials._

private object TrialSql {
  def get(id: Long): Query0[Trial] = sql"""
    SELECT id, name, trial_arm_set, sponsor
    FROM trial
    WHERE id = $id
  """.query
}

private object TrialContractSql {
  def get(id: Long): Query0[TrialContract] = sql"""
    SELECT id, name, trial_id, sponsor, fixed_general_item_set, fixed_personnel_item_set, indirect_rate, cost_per_participant
    FROM trial_contract
    WHERE id = $id
  """.query

  def getByTrialAndSponsor(trialId: Long, sponsor: Long): Query0[TrialContract] = sql"""
    SELECT id, name, trial_id, sponsor, fixed_general_item_set, fixed_personnel_item_set, indirect_rate, cost_per_participant
    FROM trial_contract
    WHERE trial_id = $trialId and sponsor = $sponsor
  """.query
}

private object TrialArmSql {
  def get(id: Long): Query0[TrialArm] = sql"""
    SELECT id, name, num_participants, variable_procedure_item_set, variable_general_item_set
    FROM trial_arm
    WHERE id = $id
  """.query
}

private object VariableProcedureItemSql {
  def get(id: Long): Query0[VariableProcedureItem] = sql"""
    SELECT id, quantity, sponsor_cost
    FROM variable_procedure_item
    WHERE id = $id
  """.query
}

private object VariableGeneralItemSql {
  def get(id: Long): Query0[VariableGeneralItem] = sql"""
    SELECT id, quantity, sponsor_cost
    FROM variable_general_item
    WHERE id = $id
  """.query
}

private object JunctionSql {
  def list(id: Long): Query0[Junction] = sql"""
    SELECT set_id, item_id
    FROM junction
    WHERE set_id = $id
  """.query
}

class DoobieTrialRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends TrialRepositoryAlgebra[F] {

  // OptionT[F, Trial] == F[Option[Trial]]
  override def get(id: Long): OptionT[F, Trial] = OptionT(TrialSql.get(id).option.transact(xa))
}

object DoobieTrialRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieTrialRepositoryInterpreter[F] =
    new DoobieTrialRepositoryInterpreter(xa)
}

class DoobieTrialArmRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends TrialArmRepositoryAlgebra[F] {

  override def get(id: Long): OptionT[F, TrialArm] = OptionT(TrialArmSql.get(id).option.transact(xa))
}

object DoobieTrialArmRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieTrialArmRepositoryInterpreter[F] =
    new DoobieTrialArmRepositoryInterpreter(xa)
}

class DoobieTrialContractRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends TrialContractRepositoryAlgebra[F] {

  override def get(id: Long): OptionT[F, TrialContract] = OptionT(TrialContractSql.get(id).option.transact(xa))

  override def getByTrialAndSponsor(trialId: Option[Long], sponsor: Option[Long]): OptionT[F, TrialContract] =
    if (trialId.isEmpty || sponsor.isEmpty)
      OptionT.none
    else
      OptionT(TrialContractSql.getByTrialAndSponsor(trialId.get, sponsor.get).option.transact(xa))
}

object DoobieTrialContractRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieTrialContractRepositoryInterpreter[F] =
    new DoobieTrialContractRepositoryInterpreter(xa)
}

class DoobieVariableProcedureItemRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends VariableProcedureItemRepositoryAlgebra[F] {

  override def get(id: Long): OptionT[F, VariableProcedureItem] = OptionT(VariableProcedureItemSql.get(id).option.transact(xa))
}

object DoobieVariableProcedureItemRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieVariableProcedureItemRepositoryInterpreter[F] =
    new DoobieVariableProcedureItemRepositoryInterpreter(xa)
}

class DoobieVariableGeneralItemRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends VariableGeneralItemRepositoryAlgebra[F] {

  override def get(id: Long): OptionT[F, VariableGeneralItem] = OptionT(VariableGeneralItemSql.get(id).option.transact(xa))
}

object DoobieVariableGeneralItemRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieVariableGeneralItemRepositoryInterpreter[F] =
    new DoobieVariableGeneralItemRepositoryInterpreter(xa)
}

class DoobieJunctionRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends JunctionRepositoryAlgebra[F] {

  override def list(id: Long): F[List[Junction]] = JunctionSql.list(id).to[List].transact(xa)

  override def list(id: Option[Long]): F[List[Junction]] =
    if (id.isEmpty)
      Applicative[F].pure(List[Junction]())
    else
      JunctionSql.list(id.get).to[List].transact(xa)
}

object DoobieJunctionRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieJunctionRepositoryInterpreter[F] =
    new DoobieJunctionRepositoryInterpreter(xa)
}
