package wraith.enchant_giver.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.enchant_giver.EnchantsList;

import java.util.HashMap;
import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = "getLevel", at = @At("HEAD"), cancellable = true)
    private static void getLevel(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        CompoundTag nbtEnchants = stack.getSubTag("EnchantGiver");
        Identifier enchant = Registry.ENCHANTMENT.getId(enchantment);
        if (nbtEnchants != null && enchant != null && nbtEnchants.contains(enchant.toString())) {
            cir.setReturnValue(nbtEnchants.getInt(enchant.toString()));
            return;
        }
        if (EnchantsList.itemHasEnchantment(Registry.ITEM.getId(stack.getItem()), enchant)) {
            cir.setReturnValue(EnchantsList.getEnchantmentLevel(Registry.ITEM.getId(stack.getItem()), Registry.ENCHANTMENT.getId(enchantment)));
        }
    }

    @ModifyVariable(method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemStack;getEnchantments()Lnet/minecraft/nbt/ListTag;"))
    private static ListTag forEachEnchantment(ListTag listTag, EnchantmentHelper.Consumer consumer, ItemStack stack) {
        ListTag newListTag = new ListTag();
        HashMap<String, Integer> enchantMap = new HashMap<>();
        for (int i = 0; i < listTag.size(); ++i) {
            String ench = listTag.getCompound(i).getString("id");
            int level = listTag.getCompound(i).getInt("lvl");
            enchantMap.put(ench, level);
        }
        for(Map.Entry<Identifier, Integer> enchantEntry : EnchantsList.getEnchantments(Registry.ITEM.getId(stack.getItem())).entrySet()) {
            String ench = enchantEntry.getKey().toString();
            int level = enchantEntry.getValue();
            enchantMap.put(ench, level);
        }
        CompoundTag nbtEnchants = stack.getSubTag("EnchantGiver");
        if (nbtEnchants != null) {
            for (String enchant : nbtEnchants.getKeys()) {
                enchantMap.put(enchant, nbtEnchants.getInt(enchant));
            }
        }
        for (Map.Entry<String, Integer> enchantEntry : enchantMap.entrySet()) {
            CompoundTag tag = new CompoundTag();
            String ench = enchantEntry.getKey();
            int level = enchantEntry.getValue();
            tag.putString("id", ench);
            tag.putInt("lvl", level);
            newListTag.add(tag);
        }
        return newListTag;
    }

}
