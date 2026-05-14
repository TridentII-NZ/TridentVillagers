package TridentII.villager;

import TridentII.config.PluginConfig;
import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public final class VillagerTradeService {

    private final PluginConfig config;
    private final BribeStorage bribeStorage;

    public VillagerTradeService(PluginConfig config, BribeStorage bribeStorage) {
        this.config = config;
        this.bribeStorage = bribeStorage;
    }

    public void restock(Villager villager) {
        villager.setRestocksToday(0);
        villager.restock();

        boolean resetUses = config.resetUses();
        boolean resetDemand = config.resetDemand();
        if (resetUses || resetDemand) {
            List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
            for (MerchantRecipe recipe : recipes) {
                if (resetUses) {
                    recipe.setUses(0);
                }
                if (resetDemand) {
                    recipe.setDemand(0);
                }
            }
            villager.setRecipes(recipes);
        }
    }

    public boolean applyBribe(Player player, Villager villager, ItemStack offeredItem) {
        for (PluginConfig.BribeOffer offer : config.bribeOffers()) {
            if (!offer.accepts(offeredItem)) {
                continue;
            }

            offeredItem.setAmount(offeredItem.getAmount() - offer.amount());
            int cureCount = bribeStorage.addCures(villager, player.getUniqueId(), offer.cureCount(), config.maxCuresPerPlayer());
            applyCureReputation(player, villager, cureCount);
            villager.updateDemand();
            restock(villager);
            return true;
        }

        return false;
    }

    public void applyStoredCures(Player player, Villager villager) {
        int cureCount = bribeStorage.cureCount(villager, player.getUniqueId());
        if (cureCount <= 0) {
            return;
        }

        applyCureReputation(player, villager, cureCount);
        villager.updateDemand();
    }

    private void applyCureReputation(Player player, Villager villager, int cureCount) {
        Reputation reputation = villager.getReputation(player.getUniqueId());
        int cureValue = Math.min(100, cureCount * config.cureReputationValue());
        reputation.setReputation(
            ReputationType.MAJOR_POSITIVE,
            Math.max(reputation.getReputation(ReputationType.MAJOR_POSITIVE), cureValue)
        );
        villager.setReputation(player.getUniqueId(), reputation);
    }
}
