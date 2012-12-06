package br.usp.ime.cassiop.workloadsim.workload;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import br.usp.ime.cassiop.workloadsim.Workload;
import br.usp.ime.cassiop.workloadsim.model.Database;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class GoogleWorkload extends Workload {

	private PreparedStatement selectDemandPs = null;

	private Database db = null;

	private String maxCpuQuery = "select max(nrml_task_cores) "
			+ " from wl_google_tasks ";

	private String maxMemQuery = "select max(nrml_task_mem) "
			+ " from wl_google_tasks ";

	public static GoogleWorkload build() throws Exception {
		long initialTime = 90000;
		long timeInterval = 300;
		long lastTime = 112500;
		// long lastTime = 90300;

		GoogleWorkload gw = new GoogleWorkload(initialTime, timeInterval,
				lastTime);

		gw.connect();

		return gw;
	}

	public GoogleWorkload(long initialTime, long timeInterval, long lastTime) {
		super(initialTime, timeInterval, lastTime);
	}

	public void connect() throws Exception {
		db = new Database();

		String query = "select task_id, nrml_task_cores, nrml_task_mem "
				+ " from wl_google_tasks " + " where time = ? ";

		try {
			db.connect();

			selectDemandPs = db.getConnection().prepareStatement(query);

		} catch (SQLException e) {
			throw new Exception("Could not prepare queries.");
		}
	}

	public List<VirtualMachine> getDemand(long time) {

		List<VirtualMachine> vmList = new LinkedList<VirtualMachine>();

		try {
			selectDemandPs.setLong(1, time);

			ResultSet rs = selectDemandPs.executeQuery();

			while (rs.next()) {
				vmList.add(resultSetToVm(rs));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return vmList;
	}

	private VirtualMachine resultSetToVm(ResultSet rs) throws SQLException {
		VirtualMachine vm = new VirtualMachine();
		vm.setName(rs.getString(1));
		vm.setDemand(ResourceType.CPU, rs.getDouble(2));
		vm.setDemand(ResourceType.MEMORY, rs.getDouble(3));

		return vm;
	}

	public double getMaxCpu() throws SQLException {
		ResultSet rs = db.getConnection().createStatement()
				.executeQuery(maxCpuQuery);
		if (rs.next()) {
			return rs.getDouble(1);
		}
		return 0;
	}

	public double getMaxMem() throws SQLException {
		ResultSet rs = db.getConnection().createStatement()
				.executeQuery(maxMemQuery);
		if (rs.next()) {
			return rs.getDouble(1);
		}
		return 0;
	}

	@Override
	public boolean hasDemand(long currentTime) {

		if (currentTime >= initialTime && currentTime <= lastTime
				&& currentTime % timeInterval == 0) {
			return true;
		}
		return false;
	}
}
