package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.interfaceService.IThiSinhService;
import com.tuyensinh.util.PasswordUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.List;
import java.util.Optional;

public class ThiSinhService implements IThiSinhService {

    private static final DateTimeFormatter DEFAULT_PASSWORD_FMT = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final ThiSinhDao dao = new ThiSinhDao();
    private final DiemThiDao diemThiDao = new DiemThiDao();
    private final NguoiDungDao nguoiDungDao = new NguoiDungDao();
    private final VaiTroDao vaiTroDao = new VaiTroDao();

    public List<ThiSinh> findAll() {
        return dao.findAll();
    }

    public List<ThiSinh> findByPage(int page, int pageSize) {
        return dao.findByPage(page, pageSize);
    }

    public List<ThiSinh> findByPageWithSearch(String keyword, int page, int pageSize) {
        return dao.findByPageWithSearch(keyword, page, pageSize);
    }

    public ThiSinh findById(Integer id) {
        return dao.findById(id);
    }

    public Optional<ThiSinh> findByCccd(String cccd) {
        return dao.findByCccd(cccd);
    }

    public Optional<ThiSinh> findBySoBaoDanh(String sobaodanh) {
        return dao.findBySoBaoDanh(sobaodanh);
    }

    @Override
    public Optional<ThiSinh> findByNguoiDungId(Integer nguoidungId) {
        if (nguoidungId == null) {
            return Optional.empty();
        }
        List<ThiSinh> list = dao.findByNguoiDungId(nguoidungId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public ThiSinh save(ThiSinh ts) {
        return dao.save(ts);
    }

    public void update(ThiSinh ts) {
        dao.update(ts);
    }

    public void delete(ThiSinh ts) {
        dao.delete(ts);
    }

    public List<ThiSinh> search(String keyword) {
        return dao.searchByCccdOrHoTen(keyword);
    }

    public long countBySearch(String keyword) {
        return dao.countBySearch(keyword);
    }

    public String generateSoBaoDanh() {
        return dao.generateSoBaoDanh();
    }

    public List<DiemThi> getDiemThiList(Integer thisinhId) {
        return diemThiDao.findByThiSinhId(thisinhId);
    }

    @Override
    public ThiSinhBulkAccountResult createBulkAccountsForImportedCandidates() {
        VaiTro userRole = vaiTroDao.findByMa(VaiTro.USER);
        if (userRole == null) {
            throw new IllegalStateException("Khong tim thay vai tro USER trong bang xt_vaitro.");
        }

        List<ThiSinh> candidates = dao.findAll().stream()
                .filter(ts -> ts.getNguoiDung() == null)
                .collect(Collectors.toList());

        Set<String> existingUsernames = nguoiDungDao.findAll().stream()
                .map(NguoiDung::getUsername)
                .map(this::normalizeUsername)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toCollection(HashSet::new));

        ThiSinhBulkAccountResult result = new ThiSinhBulkAccountResult();
        result.setTotalCandidates(candidates.size());

        for (ThiSinh ts : candidates) {
            String hoTen = safeFullName(ts);
            String username = normalizeUsername(ts.getCccd());

            if (username == null || username.isEmpty()) {
                result.incrementSkipped();
                result.addDetail(label(ts, hoTen) + " -> bo qua: thieu CCCD.");
                continue;
            }

            LocalDate ngaySinh = ts.getNgaySinh();
            if (ngaySinh == null) {
                result.incrementSkipped();
                result.addDetail(label(ts, hoTen) + " -> bo qua: thieu ngay sinh.");
                continue;
            }

            if (existingUsernames.contains(username)) {
                result.incrementSkipped();
                result.addDetail(label(ts, hoTen) + " -> bo qua: username '" + username + "' da ton tai.");
                continue;
            }

            try {
                String rawPassword = DEFAULT_PASSWORD_FMT.format(ngaySinh);

                NguoiDung nd = new NguoiDung();
                nd.setUsername(username);
                nd.setPasswordHash(PasswordUtil.hashPassword(rawPassword));
                nd.setHoTen(hoTen);
                nd.setEmail(normalizeNullable(ts.getEmail()));
                nd.setVaiTro(userRole);
                nd.setIsActive(true);

                NguoiDung savedNguoiDung = nguoiDungDao.save(nd);
                ts.setNguoiDung(savedNguoiDung);
                dao.update(ts);

                existingUsernames.add(username);
                result.incrementCreated();
                result.addDetail(label(ts, hoTen) + " -> tao thanh cong: username='" + username + "', mat khau goc='" + rawPassword + "'.");
            } catch (Exception ex) {
                result.incrementError();
                result.addDetail(label(ts, hoTen) + " -> loi: " + ex.getMessage());
            }
        }

        return result;
    }

    public int getTotalPages(long total, int pageSize) {
        return (int) Math.ceil((double) total / pageSize);
    }

    private String normalizeUsername(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("\\s+", "").trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String safeFullName(ThiSinh ts) {
        String ho = ts.getHo() != null ? ts.getHo().trim() : "";
        String ten = ts.getTen() != null ? ts.getTen().trim() : "";
        String full = (ho + " " + ten).trim();
        return full.isEmpty() ? null : full;
    }

    private String label(ThiSinh ts, String hoTen) {
        StringBuilder sb = new StringBuilder();
        if (ts.getSobaodanh() != null && !ts.getSobaodanh().trim().isEmpty()) {
            sb.append("SBD ").append(ts.getSobaodanh().trim());
        } else {
            sb.append("Thi sinh ID ").append(ts.getThisinhId());
        }
        if (hoTen != null && !hoTen.isEmpty()) {
            sb.append(" - ").append(hoTen);
        }
        return sb.toString();
    }
}
