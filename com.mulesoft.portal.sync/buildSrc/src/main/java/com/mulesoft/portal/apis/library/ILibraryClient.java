package com.mulesoft.portal.apis.library;

public interface ILibraryClient {

	public abstract LibraryNode[] getCurrentlyPublishedToLibrary();

	public abstract void updateOrCreateNode(LibraryNode node);

	public abstract void deleteNode(LibraryNode node);

}