package src.com.eztier.testbadsqlmodel
package infrastructure.repository.doobie

import cats.data.OptionT
import cats.effect.Bracket
import doobie._
import doobie.implicits._
import domain.trials._

private object TrialSql {
  def get(id: Long): Query0[Trial] = sql"""
    SELECT id, name, trial_arm_set
    FROM trial
    WHERE id = $id
  """.query
}

private object TrialContractSql {
  def get(id: Long): Query0[TrialContract] = sql"""
    SELECT id, name, trial_id, fixed_general_item_set, fixed_personnel_item_set, indirect_rate, cost_per_participant
    FROM trial_contract
    WHERE id = $id
  """.query
}

private object TrialArmSql {
  def get(id: Long): Query0[TrialArm] = sql"""
    SELECT id, name, num_participants, variable_procedure_item_set, variable_general_item_set
    FROM trial_arm
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

class DoobieTrialArmRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends TrialArmRepositoryAlgebra[F] {

  override def get(id: Long): OptionT[F, TrialArm] = OptionT(TrialArmSql.get(id).option.transact(xa))
}

class DoobieTrialContractRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends TrialContractRepositoryAlgebra[F] {

  override def get(id: Long): OptionT[F, TrialContract] = OptionT(TrialContractSql.get(id).option.transact(xa))
}

class DoobieJunctionRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends JunctionRepositoryAlgebra[F] {

  override def list(id: Long): F[List[Junction]] = JunctionSql.list(id).to[List].transact(xa)
}
