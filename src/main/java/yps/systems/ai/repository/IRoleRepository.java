package yps.systems.ai.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import yps.systems.ai.model.Role;

@Repository
public interface IRoleRepository extends Neo4jRepository<Role, String> {

    @Query("MATCH (p:Person), (r:Role) " +
            "WHERE elementId(p) = $personElementId " +
            "AND elementId(r) = $roleElementId " +
            "CREATE (p)-[:HAS_ROLE]->(r)")
    void setRoleTo(String personElementId, String roleElementId);

    @Query("MATCH (p:Person) " +
            "WHERE elementId(p) = $personElementId " +
            "MATCH (p)-[hr:HAS_ROLE]->(r) " +
            "DELETE hr")
    void deleteRoleRelation(String personElementId);

    @Query("MATCH (r:Role) " +
            "WHERE elementId(r) = $roleElementId " +
            "MATCH (p)-[hr:HAS_ROLE]->(r) " +
            "DELETE hr, r")
    void deleteRole(String roleElementId);

}
