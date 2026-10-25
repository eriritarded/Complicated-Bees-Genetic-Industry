package com.thesmartnoob880.genindustry_bees.util;

import com.accbdd.complicated_bees.port.EnergyStorage;

@SuppressWarnings("ALL")
public class customEnergyStorage extends EnergyStorage {
    public customEnergyStorage(int capacity){super(capacity);}

    public customEnergyStorage(int capacity, int maxTransfer){super(capacity, maxTransfer);}

    public customEnergyStorage(int capacity, int maxReceive, int maxExtract){super(capacity, maxReceive, maxExtract);}

    public customEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy){super(capacity, maxReceive, maxExtract, energy);}

    public void setEnergy(int energy){this.energy = energy;}

    public void addEnergy(int energy){this.energy += energy;}

    public void removeEnergy(int energy){this.energy -= energy;}
}
