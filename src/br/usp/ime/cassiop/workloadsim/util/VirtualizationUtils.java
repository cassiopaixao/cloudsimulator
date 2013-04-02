package br.usp.ime.cassiop.workloadsim.util;

import java.util.Comparator;

import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class VirtualizationUtils {

	public static class OrderByResourceUtilization implements
			Comparator<VirtualMachine> {
		@Override
		public int compare(VirtualMachine vm0, VirtualMachine vm1) {
			if (vm0.getResourceUtilization() < vm1.getResourceUtilization()) {
				return -1;
			} else if (MathUtils.equals(vm0.getResourceUtilization(),
					vm1.getResourceUtilization())) {
				return 0;
			} else {
				return 1;
			}

		}
	}
}
