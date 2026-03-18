# Prompt Editor

[English](#english) | [中文](#中文)

---

<h2 id="english">Prompt Editor</h2>

**Prompt Editor** is an Android application designed for managing and editing prompts for AI generation tools like NovelAI and Stable Diffusion.

### Features
- **Preset Management**: Create, edit, rename, and organize collections of prompt presets effortlessly.
- **Syntax Parsers**: Built-in support for multiple parser formatting including **NovelAI** and **Stable Diffusion** (e.g., prompt weights adjusting).
- **Tag Repository**: Import and manage your local tag dictionary. Easily add tags via CSV format (`tag_name, tag_description`).
- **Flexible Editing**: Switch seamlessly between List Mode and Text Mode depending on your workflow. Long press and drag to reorder prompts.
- **Tag Search & Edit**: Quickly search your repository, add, edit, or delete tags on the fly.

### Tech Stack / Built With
- **UI**: Jetpack Compose (Material Design 3)
- **Architecture**: MVI
- **Dependency Injection**: Koin
- **Local Storage**: Room & Kotpref
- **Language**: Kotlin

---

<h2 id="中文">Prompt Editor (提示词编辑器)</h2>

**Prompt Editor** 是一款专为 AI 绘画生成工具（如 NovelAI 和 Stable Diffusion）设计的 Android 提示词管理与编辑应用。

### 主要功能
- **预设编辑与管理**：轻松创建、编辑、重命名和管理你的提示词预设文件。
- **语法解析支持**：内置 **NovelAI** 和 **Stable Diffusion** 的语法解析器，方便快速调整提示词和权重格式。
- **本地标签词库**：管理个人的专属标签库。支持通过 CSV 文件一键导入庞大词库（格式：`tag_name, tag_description`）。
- **灵活的编辑模式**：支持在列表模式和纯文本模式之间无缝切换。在列表模式下，长按并拖拽即可快速对提示词重新排序。
- **标签搜索与管理**：提供便捷的搜索功能，快速在词库中定位、编辑或删除特定标签。

### 技术栈
- **UI**: Jetpack Compose (Material Design 3)
- **架构**: MVI
- **依赖注入**: Koin
- **本地存储**: Room & Kotpref
- **开发语言**: Kotlin
