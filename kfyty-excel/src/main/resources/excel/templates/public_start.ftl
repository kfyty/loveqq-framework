<#assign alphas = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"] />
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
           xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
           xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" mc:Ignorable="x14ac xr xr2 xr3"
           xmlns:x14ac="http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac"
           xmlns:xr="http://schemas.microsoft.com/office/spreadsheetml/2014/revision"
           xmlns:xr2="http://schemas.microsoft.com/office/spreadsheetml/2015/revision2"
           xmlns:xr3="http://schemas.microsoft.com/office/spreadsheetml/2016/revision3"
           xr:uid="{00000000-0001-0000-0000-000000000000}">
    <dimension ref="A1:N8"/>
    <sheetViews>
        <sheetView tabSelected="1" workbookViewId="0">
            <pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/>
            <selection pane="bottomLeft" activeCell="C3" sqref="C3"/>
        </sheetView>
    </sheetViews>
    <sheetFormatPr defaultRowHeight="14" x14ac:dyDescent="0.3"/>
    <cols>
        <col min="1" max="1" width="16.25" bestFit="1" customWidth="1"/>
        <col min="2" max="2" width="14.25" bestFit="1" customWidth="1"/>
        <col min="3" max="3" width="11.25" bestFit="1" customWidth="1"/>
        <col min="4" max="4" width="12.33203125" bestFit="1" customWidth="1"/>
        <col min="5" max="5" width="8.5" bestFit="1" customWidth="1"/>
        <col min="6" max="12" width="6.6640625" bestFit="1" customWidth="1"/>
        <col min="13" max="13" width="10.4140625" bestFit="1" customWidth="1"/>
        <col min="14" max="14" width="12.33203125" bestFit="1" customWidth="1"/>
    </cols>
    <sheetData>
        <row r="1" spans="1:14" s="4" customFormat="1" ht="25" customHeight="1" x14ac:dyDescent="0.3">
            <#list titles as title>
                <c r="${alphas[title_index]}1" s="3" t="inlineStr">
                    <is><t>${title.title}</t></is>
                </c>
            </#list>
        </row>