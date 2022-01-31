package com.synopsys.integration.detectable.detectables.cargo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.moandjiezana.toml.Toml;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.builder.MissingExternalIdException;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectables.cargo.data.CargoLockData;
import com.synopsys.integration.detectable.detectables.cargo.data.CargoTomlData;
import com.synopsys.integration.detectable.detectables.cargo.model.CargoLockPackage;
import com.synopsys.integration.detectable.detectables.cargo.transform.CargoLockPackageDataTransformer;
import com.synopsys.integration.detectable.detectables.cargo.transform.CargoLockPackageTransformer;
import com.synopsys.integration.detectable.detectables.cargo.transform.CargoTomlDataTransformer;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.util.CycleDetectedException;
import com.synopsys.integration.util.NameVersion;

public class CargoExtractor {
    private final CargoLockPackageDataTransformer cargoLockPackageDataTransformer;
    private final CargoTomlDataTransformer cargoTomlDataTransformer;
    private final CargoLockPackageTransformer cargoLockPackageTransformer;

    public CargoExtractor(CargoLockPackageDataTransformer cargoLockPackageDataTransformer, CargoTomlDataTransformer cargoTomlDataTransformer, CargoLockPackageTransformer cargoLockPackageTransformer) {
        this.cargoLockPackageDataTransformer = cargoLockPackageDataTransformer;
        this.cargoTomlDataTransformer = cargoTomlDataTransformer;
        this.cargoLockPackageTransformer = cargoLockPackageTransformer;
    }

    public Extraction extract(File cargoLockFile, @Nullable File cargoTomlFile) throws IOException, CycleDetectedException, DetectableException {
        CargoLockData cargoLockData = new Toml().read(cargoLockFile).to(CargoLockData.class);
        List<CargoLockPackage> packages = cargoLockData.getPackages()
            .orElse(new ArrayList<>()).stream()
            .map(cargoLockPackageDataTransformer::transform)
            .collect(Collectors.toList());
        DependencyGraph graph;
        try {
            graph = cargoLockPackageTransformer.transformToGraph(packages);
        } catch (MissingExternalIdException e) {
            // Wrapping because MissingExternalIdException is only accessible through integration-bdio
            throw new DetectableException(e);
        }

        Optional<NameVersion> projectNameVersion = Optional.empty();
        if (cargoTomlFile != null) {
            CargoTomlData cargoTomlData = new Toml().read(cargoTomlFile).to(CargoTomlData.class);
            projectNameVersion = cargoTomlDataTransformer.findProjectNameVersion(cargoTomlData);
        }

        CodeLocation codeLocation = new CodeLocation(graph); //TODO: Consider for 8.0.0 providing an external ID.

        return new Extraction.Builder()
            .success(codeLocation)
            .nameVersionIfPresent(projectNameVersion)
            .build();
    }
}
