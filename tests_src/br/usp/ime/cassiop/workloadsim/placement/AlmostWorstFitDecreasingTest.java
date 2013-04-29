package br.usp.ime.cassiop.workloadsim.placement;

import static br.usp.ime.cassiop.workloadsim.util.TestUtils.buildVirtualMachine;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.model.VirtualMachine;

public class AlmostWorstFitDecreasingTest {

	@Test
	public void testOrderDemand() {
		AlmostWorstFitDecreasing awfd = new AlmostWorstFitDecreasing();

		VirtualMachine vm1 = buildVirtualMachine(0.7, 0.7);
		VirtualMachine vm2 = buildVirtualMachine(0.5, 0.5);
		VirtualMachine vm3 = buildVirtualMachine(1.0, 1.0);
		VirtualMachine vm4 = buildVirtualMachine(0.7, 0.3);
		VirtualMachine vm5 = buildVirtualMachine(0.2, 1.0);

		List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
		vmList.add(vm1);
		vmList.add(vm2);
		vmList.add(vm3);
		vmList.add(vm4);
		vmList.add(vm5);

		awfd.orderDemand(vmList);

		assertTrue(vmList.get(0).equals(vm3));
		assertTrue(vmList.get(1).equals(vm1));
		assertTrue(vmList.get(2).equals(vm4));
		assertTrue(vmList.get(3).equals(vm2));
		assertTrue(vmList.get(4).equals(vm5));
	}

}
