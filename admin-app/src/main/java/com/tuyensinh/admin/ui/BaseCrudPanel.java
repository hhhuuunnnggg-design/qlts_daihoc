package com.tuyensinh.admin.ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Lớp nền generic cho tất cả panel CRUD có bảng.
 * Subclass gọi {@code initCrudUI()} và {@code loadData()} trong constructor,
 * SAU khi khởi tạo services.
 *
 * Thứ tự constructor đúng:
 * <pre>
 * public MyPanel(MainFrame mf) {
 *     super(mf);         // 1. Gọi super
 *     myService = new MyService();  // 2. Khởi tạo services
 *     initCrudUI();      // 3. Build UI (sau khi services đã sẵn sàng)
 *     loadData();        // 4. Load dữ liệu
 * }
 * </pre>
 */
public abstract class BaseCrudPanel<T> extends BasePanel {

    protected JTable table;
    protected DefaultTableModel model;
    protected JTextField searchTextField;
    protected JLabel totalLabel;
    protected JSpinner pageSpinner;

    protected int currentPage = 1;
    protected int pageSize = 20;
    protected boolean usePagination = false;

    protected BaseCrudPanel(MainFrame mainFrame) {
        super(mainFrame);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BASE IMPLEMENTATION — triển khai từ BasePanel
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void initUI() {
        buildToolbar();
        buildTable();
        buildBottomBar();
    }

    @Override
    public void loadData() {
        model.setRowCount(0);
        onDataLoaded();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ABSTRACT — subclass phải override
    // ═══════════════════════════════════════════════════════════════════

    /** Danh sách tên cột hiển thị trên bảng */
    protected abstract String[] getTableColumns();

    /** Lấy entity đang được chọn trên bảng */
    protected abstract T getSelectedEntity();

    /** Lấy ID (Integer) của entity đang được chọn */
    protected abstract Integer getSelectedId();

    // ═══════════════════════════════════════════════════════════════════
    //  OPTIONAL — override nếu cần
    // ═══════════════════════════════════════════════════════════════════

    /** Gọi sau mỗi lần loadData(). Override nếu cần cập nhật thêm. */
    protected void onDataLoaded() {}

    /** Xử lý khi chọn một dòng trên bảng */
    protected void onRowSelected() {}

    /** Override để đặt độ rộng cột. Mặc định: cột 0 = 40px. */
    protected void configureTableColumns() {
        if (table != null) {
            table.getColumnModel().getColumn(0).setPreferredWidth(40);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CRUD BUILDERS — gọi từ subclass
    // ═══════════════════════════════════════════════════════════════════

    /** Build toolbar + table + bottom bar */
    protected void initCrudUI() {
        buildToolbar();
        buildTable();
        buildBottomBar();
    }

    protected void buildToolbar() {
        JTextField[] searchFieldOut = new JTextField[1];
        JPanel toolbar = ToolbarFactory.createSearchToolbar(searchFieldOut, this::doSearch,
            new ToolbarFactory.ActionButton("Them moi", this::showAddDialog),
            new ToolbarFactory.ActionButton("Sua", this::showEditDialog),
            new ToolbarFactory.ActionButton("Xoa", this::doDelete)
        );
        this.searchTextField = searchFieldOut[0];
        add(toolbar, BorderLayout.NORTH);
    }

    protected void buildTable() {
        model = TableFactory.newReadOnlyModel(getTableColumns());
        table = TableFactory.create(model);
        configureTableColumns();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });
        add(TableFactory.wrap(table), BorderLayout.CENTER);
    }

    protected void buildBottomBar() {
        totalLabel = new JLabel("Tong: 0");
        totalLabel.setFont(UIConstants.FONT_SMALL);

        JPanel paging = null;
        if (usePagination) {
            pageSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
            paging = ToolbarFactory.createPagingPanel(pageSpinner, () -> {
                currentPage = (Integer) pageSpinner.getValue();
                loadData();
            });
        }

        add(ToolbarFactory.createBottomBar(totalLabel, paging), BorderLayout.SOUTH);
    }

    /** Cập nhật total label sau khi load dữ liệu */
    protected void updateTotalLabel(long total, String entityName) {
        if (totalLabel != null) {
            totalLabel.setText("Tong: " + total + " " + entityName);
        }
    }

    protected void updateTotalLabel(int count, String entityName) {
        updateTotalLabel((long) count, entityName);
    }

    protected void doSearch() {
        currentPage = 1;
        loadData();
    }

    protected void doDelete() {
        T entity = getSelectedEntity();
        if (entity == null) {
            showSelectRow(this);
            return;
        }
        String name = getEntityDisplayName(entity);
        if (confirmDelete(this, name) != JOptionPane.YES_OPTION) return;

        try {
            deleteEntity(entity);
            showSuccess(this, UIConstants.MSG_DELETE_SUCCESS);
            loadData();
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  DIALOG HELPERS
    // ═══════════════════════════════════════════════════════════════════

    protected void showSelectRow() {
        showSelectRow(this);
    }

    /** Hiển thị dialog với input fields */
    protected Object[] showInputDialog(Component parent, String title, Object... fields) {
        int result = JOptionPane.showConfirmDialog(parent, fields, title,
            JOptionPane.OK_CANCEL_OPTION);
        return result == JOptionPane.OK_OPTION ? fields : null;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CRUD — override để implement
    // ═══════════════════════════════════════════════════════════════════

    protected void showAddDialog() {}
    protected void showEditDialog() {}

    protected void deleteEntity(T entity) throws Exception {
        throw new UnsupportedOperationException("Override deleteEntity()");
    }

    protected String getEntityDisplayName(T entity) {
        return entity.toString();
    }
}
