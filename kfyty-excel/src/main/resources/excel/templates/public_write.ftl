<#assign alphas = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"] />
        <#list rows as row>
            <row r="${row_index + currentRow}" spans="1:14" ht="25" customHeight="1" x14ac:dyDescent="0.3">
                <#list row.cells as cell>
                    <c r="${alphas[cell_index]}${row_index + currentRow}" s="1" t="inlineStr">
                        <is><t>${cell.data}</t></is>
                    </c>
                </#list>
            </row>
        </#list>