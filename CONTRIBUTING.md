# Contributing Translations to PillPal

First off, thank you for considering contributing to PillPal! Your help in translating the app into more languages is greatly appreciated and will help make the app accessible to a wider audience.

This guide provides instructions on how to contribute translations for specific languages.

## Supported Languages for Community Translation

We are currently looking for community contributions for the following languages:

*   **Galician (gl)**
*   **Euskera (eu)** (Basque)
*   **Catalan (ca)**

## Translation Files

Translations are managed in XML files located within the app's resource directories.

*   **English (Source for Translation):**
    *   `app/src/main/res/values-en/strings.xml`
    *   This is the primary reference file. Please use the strings from this file as the source for your translations.

*   **Galician:**
    *   `app/src/main/res/values-gl/strings.xml`

*   **Euskera (Basque):**
    *   `app/src/main/res/values-eu/strings.xml`

*   **Catalan:**
    *   `app/src/main/res/values-ca/strings.xml`

## How to Translate

1.  **Identify Untranslated Strings:**
    *   Open the English `strings.xml` file (`app/src/main/res/values-en/strings.xml`).
    *   Open the `strings.xml` file for the language you wish to translate (e.g., `app/src/main/res/values-gl/strings.xml` for Galician).
    *   Compare the two files. Any string where the text content in your target language file is still in English needs translation. (Note: `app_name` should remain "PillPal").

2.  **Translate the Text Content:**
    *   Keep the XML structure and the `<string name="some_key">...</string>` tags intact. The `name` attribute (e.g., "button_save") **must not** be changed.
    *   Only translate the text content that appears *between* the `>` and `</string>` tags.

    *Example:*

    If the English file (`values-en/strings.xml`) has:
    ```xml
    <string name="button_save">Save</string>
    ```

    A Galician translation in `values-gl/strings.xml` would be (example):
    ```xml
    <string name="button_save">Gardar</string>
    ```

3.  **Handle Placeholders:**
    *   If you encounter placeholders like `%s`, `%1$d`, `%2$s`, etc., these are used by the app to insert dynamic content (like numbers or other text).
    *   These placeholders **must be preserved** in your translated string, in the correct order if multiple exist.

    *Example:*

    English:
    ```xml
    <string name="welcome_messages">Hello %1$s, you have %2$d new messages.</string>
    ```
    Spanish (example):
    ```xml
    <string name="welcome_messages">Hola %1$s, tienes %2$d mensajes nuevos.</string>
    ```

## Contribution Process

We use the standard GitHub flow for contributions:

1.  **Fork the Repository:** Create your own copy (fork) of the main PillPal repository on GitHub.
2.  **Create a New Branch:** In your forked repository, create a new branch for your translation work. Please name it descriptively, for example:
    *   `translate-galician`
    *   `translate-euskera`
    *   `translate-catalan`
3.  **Make Your Changes:** Edit the relevant `strings.xml` file for your language (e.g., `app/src/main/res/values-gl/strings.xml`) with your translations.
4.  **Commit Your Changes:** Commit your changes with a clear commit message (e.g., "Add Galician translations for settings screen").
5.  **Open a Pull Request:** Push your changes to your forked repository on GitHub and then open a Pull Request (PR) from your branch to the `main` branch (or the current development branch) of the official PillPal repository.
    *   In your Pull Request description, please mention the language you are contributing translations for.

Our team will then review your contribution, provide feedback if necessary, and merge it once it's ready.

## Questions?

If you have any questions, need clarification on a particular string, or run into any issues, please don't hesitate to:

*   **Open an Issue:** Create a new issue on the main PillPal GitHub repository. This is the preferred way to ask questions so others can benefit from the discussion as well.

Thank you again for your interest and help in making PillPal more accessible!
