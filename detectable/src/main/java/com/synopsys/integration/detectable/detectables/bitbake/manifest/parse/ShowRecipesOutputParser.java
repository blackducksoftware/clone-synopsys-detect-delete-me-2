package com.synopsys.integration.detectable.detectables.bitbake.manifest.parse;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeRecipe;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

public class ShowRecipesOutputParser {
    private final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));

    public ShowRecipesResults parse(List<String> showRecipeLines) {
        ShowRecipesResults showRecipesResults = new ShowRecipesResults();

        boolean started = false;
        BitbakeRecipe currentRecipe = null;
        for (String line : showRecipeLines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }

            if (!started && line.trim().startsWith("=== Available recipes: ===")) {
                started = true;
            } else if (started) {
                currentRecipe = parseLine(line, currentRecipe, showRecipesResults);
            }
        }

        if (currentRecipe != null) {
            showRecipesResults.addRecipe(currentRecipe);
        }

        return showRecipesResults;
    }

    private BitbakeRecipe parseLine(String line, BitbakeRecipe currentRecipe, ShowRecipesResults showRecipesResults) {
        if (line.contains(":") && !line.startsWith("  ")) {
            // Parse beginning of new component
            if (currentRecipe != null) {
                showRecipesResults.addRecipe(currentRecipe);
            }

            String recipeName = line.replace(":", "").trim();
            return new BitbakeRecipe(recipeName, new HashSet<>());
        } else if (currentRecipe != null && line.startsWith("  ")) {
            // Parse the layer and version for the current component
            String trimmedLine = line.trim();
            int indexOfFirstSpace = trimmedLine.indexOf(' ');
            int indexOfLastSpace = trimmedLine.lastIndexOf(' ');

            if (indexOfFirstSpace != -1 && indexOfLastSpace != -1 && indexOfLastSpace + 1 < trimmedLine.length()) {
                String layer = trimmedLine.substring(0, indexOfFirstSpace);
                showRecipesResults.addLayer(layer);
                currentRecipe.addLayerName(layer);
            } else {
                logger.debug(String.format("Failed to parse layer for component '%s' from line '%s'.", currentRecipe.getName(), line));
            }

            return currentRecipe;
        } else {
            logger.debug(String.format("Failed to parse line '%s'.", line));
            return currentRecipe;
        }
    }
}
