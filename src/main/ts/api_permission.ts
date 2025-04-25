import { Project, Sdk, FileSystem, Logger } from './utils';
import {ApiWriter, ApiExcelWriter} from './api_writer';
import { SystemApiRecognizer } from './api_recognizer';
import { ReporterFormat } from './configs';
import { CommandArgs } from './index'
import fs from 'fs';
import path from 'path';
import {ApiAnalyzer} from "./api_analyzer";


export class ApiPermission {
  projectName: string;
  project: Project;
  sdk: Sdk;
  formatFlag: any;
  outputPath: string;
  logTag: string;
  debugFlag: boolean;
  noRepeat: boolean;
  libPath?: string;
  isIncludeTest: boolean;
  apiWriter?: ApiWriter;

  constructor(argv: CommandArgs) {
    if (!argv.appName) {
      throw new Error('appName not found');
    }
    this.projectName = argv.appName;
    let appProject: string | undefined;
    // if (argv.app) {
    //   appProject = argv.app;
    // } else if (argv.dir) {
    //   appProject = argv.dir;
    // }
    appProject = argv.appDir;

    if (!appProject) {
      throw new Error('app not found');
    }
    this.project = new Project(appProject, false);
    this.sdk = new Sdk(this.project, argv.sdk, undefined);
    this.formatFlag = ReporterFormat.getFlag(argv.format);
    this.outputPath = argv.output ?? appProject;
    this.logTag = 'ApiPermission';
    this.debugFlag = argv.debug || false;
    this.noRepeat = argv.noRepeat || false;
    this.isIncludeTest = argv.scanTest || false;
  }

  setLibPath(libPath: string): this {
    this.libPath = libPath;
    if (libPath && !fs.existsSync(this.libPath)) {
      Logger.warn(this.logTag, `${libPath} is not exist`);
    } else {
      Logger.info(this.logTag, `set lib path ${libPath}`);
    }
    return this;
  }

  setIncludeTest(isIncludeTest: boolean): this {
    this.isIncludeTest = isIncludeTest;
    return this;
  }

  async start(): Promise<void> {
    const sdkPath = this.sdk.getPath();
    if (!sdkPath || !fs.existsSync(sdkPath)) {
      return;
    }
    const handleFilePath = path.join(sdkPath, '/api/@internal/full/global.d.ts');
    const originalContent = fs.readFileSync(handleFilePath, 'utf-8');
    // 去除import和export关键字
    let newContent = originalContent.replace(/import|export/g, '');
    fs.writeFileSync(handleFilePath, newContent);
    Logger.info(this.logTag, `scan app ${this.project.getPath()}`);
    Logger.info(this.logTag, `sdk is in ${sdkPath}`);
    const apiLibs = this.sdk.getApiLibs();
    const componentLibs = this.sdk.getComponentLibs();
    const eslibs = this.sdk.getESLibs(this.libPath!);
    const appSourceSet = this.project.getAppSources(this.isIncludeTest);

    let systemApiRecognizer = new SystemApiRecognizer(this.projectName, this.project.getPath(), sdkPath);
    systemApiRecognizer.buildScene();
    Logger.info(this.logTag, `start scanning ${this.project.getPath()}`);
    appSourceSet.forEach((appCodeFilePath) => {
      if (this.debugFlag) {
        Logger.info(this.logTag, `scan ${appCodeFilePath}`);
      }
      systemApiRecognizer.recognize(appCodeFilePath);
    });
    Logger.info(this.logTag, `end scan ${this.project.getPath()}`);
    const apiWriter = this.getApiWriter();
    apiWriter.add(systemApiRecognizer.getApiInformations());
    // avoid oom
    // TODO: fix type error
    // systemApiRecognizer = undefined;
    await apiWriter.flush();
    fs.writeFileSync(handleFilePath, originalContent);
    // let sdk_version = this.project.getAppSdkVersion();
    let apiAnalyzer = new ApiAnalyzer(systemApiRecognizer.apiInfos, "12");
    await apiAnalyzer.analyze();
  }

  getApiWriter(): ApiWriter {
    if (!this.apiWriter) {
      this.apiWriter = new ApiWriter(this.outputPath, this.formatFlag, this.noRepeat);
    }
    return this.apiWriter;
  }

  setApiWriter(apiWriter: ApiWriter): void {
    this.apiWriter = apiWriter;
  }
}


export class MultiProjectApiPermission {
  argv: CommandArgs;
  libPath?: string;
  isIncludeTest?: boolean;
  logTag = 'MultiProjectApiPermission';

  constructor(argv: CommandArgs) {
    this.argv = argv;
  }

  setLibPath(libPath: string): this {
    this.libPath = libPath;
    if (libPath && !fs.existsSync(this.libPath)) {
      Logger.warn(this.logTag, `${libPath} is not exist`);
    } else {
      Logger.info(this.logTag, `set lib path ${libPath}`);
    }
    return this;
  }

  setIncludeTest(isIncludeTest: boolean): this {
    this.isIncludeTest = isIncludeTest;
    return this;
  }

  async start(): Promise<void> {
    const allApps = FileSystem.listAllAppDirs(this.argv.appDir!);
    if (allApps.length === 0) {
      Logger.info('MultiProjectApiPermission', `project not found in ${this.argv.appDir}`);
      return;
    }
    const output = this.argv.output ?? this.argv.appDir!;
    const apiExcelWriter = new ApiExcelWriter(output, true);
    apiExcelWriter.close();
    allApps.forEach((app) => {
      if (app) {
        this.argv.appDir = app;
        const apiCollector = new ApiPermission(this.argv);
        // TODO: fix type error
        apiCollector.setApiWriter(<ApiWriter><unknown>apiExcelWriter);
        apiCollector.setLibPath(this.libPath!).setIncludeTest(this.isIncludeTest!).start();
      }
    });
    apiExcelWriter.open();
    await apiExcelWriter.flush();
  }
}
