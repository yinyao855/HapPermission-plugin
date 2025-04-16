import {ApiDeclarationInformation} from "./api_recognizer";
import {Logger} from "./utils";
import {join} from "path";
import fs from "fs";
import csv from "csv-parser";

/**
 * 权限API分析器
 */
export class ApiAnalyzer {
    apiInfos: ApiDeclarationInformation[];
    apiPermissionMap: Map<string, string>;
    sdkVersion: string = "14";

    constructor(apiInfos: ApiDeclarationInformation[], sdkVersion: string) {
        this.apiInfos = apiInfos;
        this.apiPermissionMap = new Map<string, string>();
        this.sdkVersion = sdkVersion;
    }

    setSdkVersion(sdkVersion: string) {
        this.sdkVersion = sdkVersion;
    }

    initApiPermissionMap(): Promise<void> {
        this.apiPermissionMap.clear()
        const filePath = join(__dirname, `../resources/PerAPI/${this.sdkVersion}.csv`);

        return new Promise((resolve, reject) => {
            fs.createReadStream(filePath)
                .pipe(csv())
                .on('data', (row) => {
                    const qualifiedName = `[${row['模块名']},${row['类名']},${row['方法名']},${row['函数']}]`;
                    const permission = row['权限'];
                    this.apiPermissionMap.set(qualifiedName, permission);
                })
                .on('end', () => {
                    Logger.info('ApiAnalyzer', `find ${this.apiPermissionMap.size} permission apis in CSV file`);
                    resolve();
                })
                .on('error', (error) => {
                    Logger.error('ApiAnalyzer', `Error reading CSV file: ${error}`);
                    reject(error);
                });
        });
    }

    async analyze() {
        await this.initApiPermissionMap()
        let permissionList: string[] = [];
        for (const apiInfo of this.apiInfos) {
            const qualifiedName = `[${apiInfo.packageName},${apiInfo.typeName},${apiInfo.propertyName},${apiInfo.apiRawText}]`;
            const permission = this.apiPermissionMap.get(qualifiedName);
            if (permission) {
                permissionList.push(permission);
            }
        }
        console.log(permissionList);
        Logger.info('ApiAnalyzer', 'end analyze api permission');
    }
}