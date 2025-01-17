/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report.reporter;

import clusterless.cls.printer.Printer;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class CSVReporter<T> extends Reporter<T> {
    private final CsvSchema schema;
    private CsvMapper mapper = new CsvMapper();

    public CSVReporter(Printer printer, Class<T> type) {
        super(printer, type);

        schema = mapper.schemaFor(type)
                .withHeader();

        objectWriter = mapper.writer(schema);
    }
}
