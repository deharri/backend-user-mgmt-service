package com.deharri.ums.agency;

import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.agency.entity.AgencyMember;
import com.deharri.ums.user.entity.CoreUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgencyMemberRepository extends JpaRepository<AgencyMember, UUID> {

    Optional<AgencyMember> findByAgencyAndCoreUser(Agency agency, CoreUser coreUser);

    List<AgencyMember> findByAgency(Agency agency);

    List<AgencyMember> findByCoreUser(CoreUser coreUser);

    boolean existsByAgencyAndCoreUser(Agency agency, CoreUser coreUser);

}
