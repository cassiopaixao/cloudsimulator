package br.usp.ime.cassiop.workloadsim.workload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.cassiop.workloadsim.Workload;
import br.usp.ime.cassiop.workloadsim.model.ResourceType;
import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class GoogleClusterdataFileWorkload extends Workload {

	final Logger logger = LoggerFactory
			.getLogger(GoogleClusterdataFileWorkload.class);

	// private static final Charset charset = Charset.forName("UTF-8");

	// private String filenamePattern =
	// "/var/tmp/cassiop/res/workloads/%d-%d.csv";
	private String filenamePattern = "res/workloads/%d-%d.csv";

	public static GoogleClusterdataFileWorkload build() throws Exception {
		long initialTime = 600;
		long timeInterval = 300;
		 long lastTime = 1800; // 1200 20min de workload
		// long lastTime = 4200; // 4200 1h de workload
		// long lastTime = 25800; // 25800 7h de workload
//		long lastTime = 87000; // 87000 24h de workload

		GoogleClusterdataFileWorkload gw = new GoogleClusterdataFileWorkload(
				initialTime, timeInterval, lastTime);

		return gw;
	}

	public GoogleClusterdataFileWorkload(long initialTime, long timeInterval,
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
			logger.error("Couldn't read file: {}",
					String.format(filenamePattern, time, time + timeInterval));
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				logger.error(
						"Couldn't read file: {}",
						String.format(filenamePattern, time, time
								+ timeInterval));
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
}
