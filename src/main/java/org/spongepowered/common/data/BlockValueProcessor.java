package org.spongepowered.common.data;

import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.data.value.BaseValue;

public interface BlockValueProcessor<E, V extends BaseValue<E>> {

    E getValueForBlockState(IBlockState blockState);

    V getApiValueForBlockState(IBlockState blockState);



}
