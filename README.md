# Report Book Generator

## Background

This project was developed as part of my software engineering apprenticeship.

Since Germany`s law requires creating a report book (Berichtsheft) for the apprenticeship, one needed to be created.
The online variant was sadly not available, so a Word template from the IHK was used.
This template was really annoying to use to this app was created to create them easily.

## Usage

`de.siba.reportbookgen.ReportBookGenerator` allows templating weekly report book `docx` files.

The weekly activities then used for templating are defined in `JSON` files.

### Creating JSON Files

Create a directory that will later contain the JSON files. The files will be searched recursively so subdirectories can
be used.

The `JSON` files looks like this:

```json
{
  "number": "1",
  "activity": [
    "Created an app",
    "Updated another app"
  ],
  "activity_hours": "20",
  "teachings": [
    "Instructions using an app"
  ],
  "teachings_hours": "4",
  "school": {
    "AWP": "Some topic",
    "IT-T": "",
    "IT-S": "",
    "D": "",
    "E": "",
    "Eth": "",
    "PuG": "",
    "BP": "",
    "": ""
  },
  "school_days": "2"
}
```

Description:

| Property        | Description                                                                                                                           |
|-----------------|---------------------------------------------------------------------------------------------------------------------------------------|
| number          | Optional. Used to indicate which week is described. If not provided, the JSON file has to be named like the number e.g. `1.json`      |
| activity        | Required. List of activities that were done this week.                                                                                |
| activity_hours  | Required. Hours spent on these activities.                                                                                            |
| teachings       | Required. List of teachings received.                                                                                                 |
| teachings_hours | Required. Hours spent on these teachings.                                                                                             |
| school          | Map of school subjects and the topics learned. Leaving the value empty will be filtered out later. This supports easier copy pasting. |
| school_days     | Days spent in school.                                                                                                                 |

### Creating Word Templates

The project is based on the template that can be found [here](assets/vorlage-jahr1.docx).

You can template the Word files by using `${variableName}`. The following variables are supported:

| Variable Name   | Description                                                              |
|-----------------|--------------------------------------------------------------------------|
| number          | Number of the week.                                                      |
| year            | Apprenticship year.                                                      |
| week_start      | Start date of the week.                                                  |
| week_end        | End date of the week. (Friday)                                           |
| activity        | `acitivity` from JSON joined with `, `                                   |
| activity_hours  |                                                                          |
| teachings       | `teachings` from JSON joined with `, `                                   |
| teachings_hours |                                                                          |
| school          | `school` from JSON. Format: `$subject1: $value1; $subject2: $value2;...` |
| school_hours    |                                                                          |

### Generate Word Files

To generate the Word files from your JSON data, use the `ReportBookGenerator` command-line tool.

Example usage:

```bash
java -jar report-book-generator-1.0.0-all.jar \
  -i ./report-jsons \
  -o ./report-book \
  -y 1=2023-08-29 -y 2=2024-09-01 -y 3=2025-09-01 \
  -t "1=./templates/year1-template.docx" \
  -t "2=./templates/year2-template.docx" \
  -t "3=./templates/year3-template.docx"
```

There are additional validation options available. See the help for more information.

## Limitations

- New lines are not supported.
    - Variable replacement of `docx4j` seem to not support new lines. Keep that in mind. 