package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.interfaceService.INguoiDungService;

import java.util.List;
import java.util.Optional;

public class NguoiDungService implements INguoiDungService {

    private final NguoiDungDao dao = new NguoiDungDao();

    public List<NguoiDung> findAll() {
        return dao.findAll();
    }

    public List<NguoiDung> findByPage(int page, int pageSize) {
        return dao.findByPage(page, pageSize);
    }

    public NguoiDung findById(Integer id) {
        return dao.findById(id);
    }

    public Optional<NguoiDung> findByUsername(String username) {
        return dao.findByUsername(username);
    }

    public NguoiDung save(NguoiDung nd) {
        return dao.save(nd);
    }

    public void update(NguoiDung nd) {
        dao.update(nd);
    }

    public void delete(NguoiDung nd) {
        dao.delete(nd);
    }

    public void toggleActive(NguoiDung nd) {
        dao.toggleActive(nd);
    }

    public void changeRole(NguoiDung nd, VaiTro newRole) {
        nd.setVaiTro(newRole);
        dao.update(nd);
    }

    public List<NguoiDung> search(String keyword) {
        return dao.searchByUsernameOrHoTen(keyword);
    }

    public List<NguoiDung> findByRole(Short vaitroId) {
        return dao.findByVaiTro(vaitroId);
    }

    public List<NguoiDung> findActiveUsers() {
        return dao.findActiveUsers();
    }

    public long countAll() {
        return dao.countAll();
    }

    public int getTotalPages(long total, int pageSize) {
        return (int) Math.ceil((double) total / pageSize);
    }

    public void updatePassword(NguoiDung nd, String newPassword) {
        dao.updatePassword(nd, newPassword);
    }
}
