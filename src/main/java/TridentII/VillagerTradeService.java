package TridentII;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

final class VillagerTradeService {

    private final PluginConfig config;

    VillagerTradeService(PluginConfig config) {
        this.config = config;
    }

    void restock(Villager villager) {
        villager.setRestocksToday(0);
        villager.restock();

        if (config.bool("restock.reset-uses") || config.bool("restock.reset-demand")) {
            List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
            for (MerchantRecipe recipe : recipes) {
                if (config.bool("restock.reset-uses")) {
                    recipe.setUses(0);
                }
                if (config.bool("restock.reset-demand")) {
                    recipe.setDemand(0);
                }
            }
            villager.setRecipes(recipes);
        }
    }

    boolean applyBribe(Player player, Villager villager, ItemStack offeredItem) {
        for (PluginConfig.BribeOffer offer : config.bribeOffers()) {
            if (!offer.accepts(offeredItem)) {
                continue;
            }

            offeredItem.setAmount(offeredItem.getAmount() - offer.amount());
            improveReputation(player, villager, offer.majorPositiveReputation());
            applyRecipeDiscount(villager, offer.specialPriceDiscount());
            villager.updateDemand();
            restock(villager);
            return true;
        }

        return false;
    }

    private void improveReputation(Player player, Villager villager, int amount) {
        Reputation reputation = villager.getReputation(player.getUniqueId());
        reputation.setReputation(
            ReputationType.MAJOR_POSITIVE,
            reputation.getReputation(ReputationType.MAJOR_POSITIVE) + amount
        );
        villager.setReputation(player.getUniqueId(), reputation);
    }

    private void applyRecipeDiscount(Villager villager, int discount) {
        if (discount <= 0) {
            return;
        }

        List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
        for (MerchantRecipe recipe : recipes) {
            recipe.setIgnoreDiscounts(false);
            recipe.setSpecialPrice(recipe.getSpecialPrice() - discount);
        }
        villager.setRecipes(recipes);
    }
}
