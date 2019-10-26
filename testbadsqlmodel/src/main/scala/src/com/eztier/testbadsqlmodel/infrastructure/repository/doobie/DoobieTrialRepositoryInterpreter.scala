package src.com.eztier.testbadsqlmodel
package infrastructure.repository.doobie

import doobie._
import doobie.implicits._

import domain.trials._

private object TrialSql {
  def select(id: Long): Query0[Trial] = sql"""
    SELECT id, name, trial_arm_set
    FROM trial
    WHERE id = $id
  """.query
}

private object TrialContractSql {
  def select(id: Long): Query0[TrialContract] = sql"""
    SELECT id, name, trial_id, fixed_general_item_set, fixed_personnel_item_set, indirect_rate, cost_per_participant
    FROM trial_contract
    WHERE id = $id
  """.query
}

private object TrialArmSql {
  def select(id: Long): Query0[TrialArm] = sql"""
    SELECT id, name, num_participants, variable_procedure_item_set, variable_general_item_set
    FROM trial_arm
    WHERE id = $id
  """.query
}

private object JunctionSql {
  def select(id: Long): Query0[Junction] = sql"""
    SELECT set_id, item_id
    FROM junction
    WHERE set_id = $id
  """.query
}

class DoobieTrialRepositoryInterpreter {


}
