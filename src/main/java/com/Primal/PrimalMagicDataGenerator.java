package com.Primal;

import com.Primal.datagen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class PrimalMagicDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		// 必须包含以下这些行，否则 runDatagen 不会帮你生成文件
		pack.addProvider(ModBlockTagesProvider::new);
		pack.addProvider(ModItemTagsProvider::new);
		pack.addProvider(ModLootTableProvider::new);
		pack.addProvider(ModModelsProvider::new);
		pack.addProvider(ModRecipesProvider::new);

		//语言文件生成
		// 在 PrimalMagicDataGenerator.java 里的 onInitializeDataGenerator 方法中
		pack.addProvider(ModZHCNLanProvider::new); // 确保这一行存在
		//pack.addProvider(ModENUSLanProvider::new); // 英文同理

		// 这一行非常重要！它负责生成世界生成的 JSON
		pack.addProvider(ModWorldGenerator::new);
	}

	// 1.21 需要这个方法来注册动态注册表（如矿石生成）
	@Override
	public void buildRegistry(net.minecraft.registry.RegistryBuilder registryBuilder) {
		registryBuilder.addRegistry(net.minecraft.registry.RegistryKeys.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap);
		registryBuilder.addRegistry(net.minecraft.registry.RegistryKeys.PLACED_FEATURE, ModPlacedFeatures::bootstrap);
	}
}