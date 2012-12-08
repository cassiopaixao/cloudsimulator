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

public class GoogleClusterdataWorkload extends Workload {

	private PreparedStatement selectDemandPs = null;

	private Database db = null;

	public static GoogleClusterdataWorkload build() throws Exception {
		long initialTime = 600;
		long timeInterval = 300;
//		long lastTime = 25200;
		long lastTime = 2100;
		
		GoogleClusterdataWorkload gw = new GoogleClusterdataWorkload(
				initialTime, timeInterval, lastTime);

		gw.connect();

		return gw;
	}

	public GoogleClusterdataWorkload(long initialTime, long timeInterval,
			long lastTime) {
		super(initialTime, timeInterval, lastTime);
	}

	public void connect() throws Exception {
		db = new Database();

		String query = "select job_id, task_index, max(cpu_usage), max(memory_usage) "
				+ " from wl_clusterdata_tasks "
				+ " where start_time >= ? and end_time <= ? "
				+ " group by job_id, task_index ";

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
			selectDemandPs.setLong(2, time + timeInterval);

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
		vm.setName(rs.getString(1).concat("-").concat(rs.getString(2)));
		vm.setDemand(ResourceType.CPU, rs.getDouble(3));
		vm.setDemand(ResourceType.MEMORY, rs.getDouble(4));

		return vm;
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
