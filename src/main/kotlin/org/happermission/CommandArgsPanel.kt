package org.happermission

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.util.ui.JBUI
import org.happermission.HaperConfigService.runCommand
import org.happermission.HaperConfigService.save
import javax.swing.JComponent
import javax.swing.JTextArea
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

@OptIn(ExperimentalStdlibApi::class)
fun createCommandArgsPanel(
    project: Project,
    toolWindow: ToolWindow,
    outputContent: Content
): JComponent {
    lateinit var panel: DialogPanel

    panel = panel {
        val args = HaperConfigService.load(project)
        val libDir = ResourceExtractor.extractLibFolder("org.happermission.plugin")

        row {
            panel {
                group("基本配置") {
                    row("应用名称") {
                        textField()
                            .bindText(args::appName)
                            .columns(30)
                            .widthGroup("1")
                    }
                    row("应用路径") {
                        textFieldWithBrowseButton(
                            browseDialogTitle = "选择应用目录",
                            project = project,
                            fileChooserDescriptor = FileChooserDescriptor(
                                false,
                                true,
                                false,
                                false,
                                false,
                                false
                            )
                        )
                            .bindText(args::appDir)
                            .columns(30)
                    }
                    row("SDK 路径") {
                        textFieldWithBrowseButton(
                            browseDialogTitle = "选择应用目录",
                            project = project,
                            fileChooserDescriptor = FileChooserDescriptor(
                                false,
                                true,
                                false,
                                false,
                                false,
                                false
                            )
                        )
                            .bindText(args::sdk)
                            .columns(30)
                    }.rowComment("例：/Users/pp82/Library/OpenHarmony/Sdk/15/ets")
                    row("报告输出路径") {
                        textFieldWithBrowseButton(
                            browseDialogTitle = "选择输出位置",
                            project = project,
                            fileChooserDescriptor = FileChooserDescriptor(
                                false,
                                true,
                                false,
                                false,
                                false,
                                false
                            )
                        )
                            .bindText(args::output)
                            .columns(30)
                    }
                    row("报告格式") {
                        comboBox(Format.entries)
                            .bindItem(args::format.toNullableProperty())
                    }
                }
            }
                .gap(RightGap.COLUMNS)
                .align(AlignY.TOP)
                .resizableColumn()

            panel {
                group("高级选项") {
                    row {
                        checkBox("包含测试文件夹")
                            .bindSelected(args::scanTest)
                            .widthGroup("1")
                    }
                    row {
                        checkBox("开启调试模式")
                            .bindSelected(args::debug)
                    }
                    row {
                        checkBox("去重")
                            .bindSelected(args::noRepeat)
                    }.bottomGap(BottomGap.MEDIUM)
                    row {
                        button("运行") {
                            toolWindow.contentManager.setSelectedContent(outputContent)
                            panel.apply()
                            save(args, project)

                            val script = libDir.resolve("main.js").toString()
                            val basePath = Path(project.basePath ?: "")

                            runCommand(script, basePath, toolWindow)
                        }
                            .align(AlignX.CENTER)
                    }
                }
            }
                .align(AlignY.TOP)
                .resizableColumn()
        }
    }.withBorder(JBUI.Borders.empty(16))

    return JBScrollPane(panel)
}

