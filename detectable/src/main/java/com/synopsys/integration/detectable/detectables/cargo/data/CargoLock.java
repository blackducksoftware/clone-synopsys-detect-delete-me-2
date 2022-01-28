package com.synopsys.integration.detectable.detectables.cargo.data;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

public class CargoLock {
    @Nullable
    @SerializedName("package")
    private final List<CargoLockPackageData> packages;

    public CargoLock(@Nullable List<CargoLockPackageData> packages) {this.packages = packages;}

    public Optional<List<CargoLockPackageData>> getPackages() {
        return Optional.ofNullable(packages);
    }
}
