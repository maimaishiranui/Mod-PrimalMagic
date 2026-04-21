# PrimalMagic (原始魔法)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21-green)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.102.0+-blue)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-CC0--1.0-lightgrey)](LICENSE)

一个为 Minecraft 1.21 版本设计的魔法主题 Fabric Mod。探索全新的魔法世界，发现强大的装备、武器和神秘维度！

## ✨ 特性

- 🪄 **全新魔法系统** - 体验独特的魔法机制
- ⚔️ **新装备与武器** - 包括法杖（Sceptre）和古代钥匙（Ancient Key）等魔法物品
- 🌍 **新维度** - 探索充满魔法的神秘维度
- 🌿 **新群系** - 在魔法维度中发现独特的生态环境
- 🦎 **新生物** - 邂逅各种魔法生物
- 📦 **数据生成支持** - 内置完整的数据生成器（战利品表、配方、语言文件等）
- 🎨 **GeckoLib 动画** - 使用 GeckoLib 实现流畅的物品和实体动画

## 📋 依赖要求

- **Minecraft**: 1.21
- **Java**: 21+
- **Fabric Loader**: 0.16.14+
- **Fabric API**: 0.102.0+
- **GeckoLib**: 4.5.8+

## 🚀 安装方法

### 玩家安装

1. 安装 [Fabric Loader](https://fabricmc.net/use/)
2. 下载最新版本的 PrimalMagic
3. 将 `.jar` 文件放入 `.minecraft/mods` 文件夹
4. 启动游戏并享受魔法之旅！

### 开发者设置

```bash
# 克隆仓库
git clone https://github.com/your-username/primalmagic.git
cd primalmagic

# 构建项目
./gradlew build

# 运行客户端
./gradlew runClient

# 运行服务器
./gradlew runServer
```

## 🛠️ 开发

### 环境要求

- JDK 21 或更高版本
- Gradle 8.x（项目已包含 Gradle Wrapper）

### 数据生成

本模组包含完整的数据生成器，用于生成：
- 方块和物品标签
- 战利品表
- 合成配方
- 多语言支持（英文和中文）
- 模型定义

运行数据生成：
```bash
./gradlew runDatagen
```

## 📁 项目结构

```
src/main/java/com/Primal/
├── item/           # 自定义物品（法杖、古代钥匙等）
├── screen/         # 自定义 GUI（魔法绑定台等）
├── datagen/        # 数据生成器
├── component/      # 数据组件
└── mixin/          # Mixin 修改
```

## 🤝 贡献

欢迎贡献代码、报告问题或提出建议！

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 [CC0-1.0](LICENSE) 许可证。

## 👥 作者

- **Mariah shiranui**

## 🔗 链接

- [源代码仓库](https://github.com/FabricMC/fabric-example-mod)
- [Fabric 官网](https://fabricmc.net/)
- [GeckoLib 官网](https://geckolib.com/)

---

⚠️ **注意**: 本模组目前处于早期开发阶段（版本 0.1a），部分功能可能尚未完成。

