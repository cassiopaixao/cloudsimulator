package br.usp.ime.cassiop.workloadsim.workload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import br.usp.ime.cassiop.workloadsim.Workload;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class TestWorkload extends Workload {

	// private static final Charset charset = Charset.forName("UTF-8");

	// private String filenamePattern =
	// "/var/tmp/cassiop/res/workloads/%d-%d.csv";
	private String filenamePattern = "res/workloads_tests/%d-%d.csv";

	public static TestWorkload build() throws Exception {
		long initialTime = 600;
		long timeInterval = 300;
		long lastTime = 900;

		TestWorkload gw = new TestWorkload(
				initialTime, timeInterval, lastTime);

		return gw;
	}

	public TestWorkload(long initialTime, long timeInterval,
			long lastTime) {
		super(initialTime, timeInterval, lastTime);
	}

	public List<VirtualMachine> getDemand(long time) {

		List<VirtualMachine> vmList = new LinkedList<VirtualMachine>();

		File file = null;
		BufferedReader reader = null;
		try {
			file = new File(String.format(filenamePattern, time, time
					+ timeInterval));
			reader = new BufferedReader(new FileReader(file));

			String line = null;
			while ((line = reader.readLine()) != null) {
				vmList.add(lineToVm(line));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return vmList;
	}

	private VirtualMachine lineToVm(String line) {
		VirtualMachine vm = new VirtualMachine();

		String[] data = line.split("\t");

		vm.setEndTime(Long.parseLong(data[1]));
		vm.setName(String.format("%s-%s", data[2], data[3]));
		vm.setDemand(ResourceType.CPU, Double.parseDouble(data[4]));
		vm.setDemand(ResourceType.MEMORY, Double.parseDouble(data[5]));

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
