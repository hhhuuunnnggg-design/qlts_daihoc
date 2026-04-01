package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.NganhToHop;
import java.util.List;
import java.util.Optional;

public interface INganhToHopDao extends IBaseDao<NganhToHop> {

    List<NganhToHop> findByNganhId(Integer nganhId);

    List<NganhToHop> findByToHopId(Integer tohopId);

    Optional<NganhToHop> findByNganhAndToHop(Integer nganhId, Integer tohopId);
}
