package src.com.eztier.testbadsqlmodel
package domain.trials

case class Junction
(
  setId: Long,
  itemId: Long
)

trait JunctionRepositoryAlgebra[F[_]] {
  def list(id: Long): F[List[Junction]]
}
